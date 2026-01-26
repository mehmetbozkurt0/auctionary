package api

import (
	"context"
	"encoding/json"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/mehmetbozkurt0/auctionary/internal/models"
	"github.com/mehmetbozkurt0/auctionary/internal/repository"
	"github.com/redis/go-redis/v9"
)

var jwtKey = []byte("cok_gizli_anahtar_123")

type Handler struct {
	Repo  *repository.PostgresDB
	Redis *redis.Client
}

func NewHandler(repo *repository.PostgresDB, rdb *redis.Client) *Handler {
	return &Handler{Repo: repo, Redis: rdb}
}

func (h *Handler) Register(c *gin.Context) {
	var req models.RegisterRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid data format!"})
		return
	}
	user := &models.User{Username: req.Username, Email: req.Email, Password: req.Password}
	if err := h.Repo.CreateUser(user); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "User cannot created!"})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "User created.", "user_id": user.ID})
}

func (h *Handler) Login(c *gin.Context) {
	var req models.LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "JSON format is corrupted!"})
		return
	}

	user, err := h.Repo.GetUserByEmail(req.Email)
	if err != nil || user.Password != req.Password {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "Email or password is incorrect!"})
		return
	}

	expirationTime := time.Now().Add(24 * time.Hour)
	claims := &jwt.MapClaims{
		"user_id":  user.ID,
		"username": user.Username,
		"exp":      expirationTime.Unix(),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, _ := token.SignedString(jwtKey)

	c.JSON(http.StatusOK, gin.H{"token": tokenString, "user_id": user.ID, "username": user.Username})
}


func (h *Handler) CreateAuction(c *gin.Context) {
	var auction models.Auction
	if err := c.ShouldBindJSON(&auction); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Geçersiz veri formatı!"})
		return
	}

	auction.CurrentPrice = auction.StartingPrice
	auction.IsActive = true
	auction.EndTime = time.Now().Add(3 * time.Minute)

	if err := h.Repo.CreateNewAuction(&auction); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Veritabanı hatası: " + err.Error()})
		return
	}

	data, _ := json.Marshal(auction)
	if err := h.Redis.Set(context.Background(), "auction:"+auction.ID, data, 0).Err(); err != nil {}

	c.JSON(http.StatusCreated, gin.H{"message": "Açık artırma başlatıldı!", "auction": auction})
}

func (h *Handler) ListAuctions(c *gin.Context) {
	auctions, err := h.Repo.GetAllAuctions()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Listeleme hatası!"})
		return
	}

	for i, a := range auctions {
		val, err := h.Redis.Get(context.Background(), "auction:"+a.ID).Result()

		if err == nil {
			var redisAuction models.Auction
			if err := json.Unmarshal([]byte(val), &redisAuction); err == nil {
				auctions[i].CurrentPrice = redisAuction.CurrentPrice
				auctions[i].WinnerID = redisAuction.WinnerID
				auctions[i].EndTime = redisAuction.EndTime // Süre uzamış olabilir
				auctions[i].IsActive = redisAuction.IsActive
			}
		}
	}

	c.JSON(http.StatusOK, auctions)
}