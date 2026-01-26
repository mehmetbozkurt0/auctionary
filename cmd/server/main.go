package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"github.com/mehmetbozkurt0/auctionary/internal/api"
	"github.com/mehmetbozkurt0/auctionary/internal/models"
	"github.com/mehmetbozkurt0/auctionary/internal/repository"
	"github.com/mehmetbozkurt0/auctionary/internal/service"
	"github.com/redis/go-redis/v9"
)

var (
	rdb *redis.Client
	biddingService *service.BiddingService
	pgDB *repository.PostgresDB
	upgrader = websocket.Upgrader{
		CheckOrigin: func(r *http.Request) bool {return true},
	}
	clients = make(map[*websocket.Conn]bool)
	clientsMu sync.Mutex
)

func ginWebSocketHandler(c *gin.Context){
	handleWebSocket(c.Writer, c.Request)
}

func checkAuctions() {
	ticker := time.NewTicker(1*time.Second)
	for range ticker.C {
		keys, err := rdb.Keys(repository.Ctx, "auction:*").Result()
		if err != nil {
			continue
		}

		for _, key := range keys{
			val, err := rdb.Get(repository.Ctx, key).Result()
			if err != nil {
				continue
			}

			var auction *models.Auction
			if err := json.Unmarshal([]byte(val), &auction); err != nil {
				continue
			}

			if !auction.IsActive{
				continue
			}

			if time.Now().After(auction.EndTime){
				fmt.Printf("Time's up! %s - Winner: %s\n",auction.ProductName, auction.WinnerID)

				auction.IsActive = false
				updateData, _ := json.Marshal(auction)
				rdb.Set(repository.Ctx, key, updateData, 0)
				pgDB.SaveFinishedAuction(auction)

				endEvent := models.BaseEvent{
					Type: models.EventAuctionEnd,
					Payload: map[string]interface{}{
						"auction_id": auction.ID,
						"winner_id": auction.WinnerID,
						"final_price": auction.CurrentPrice,
					},
				}
				repository.PublishEvent(rdb, "auction_events", endEvent)
			}
		}
	}
}

func subscribeToAuctionEvents() {
	pubsub := rdb.Subscribe(repository.Ctx, "auction_events")
	ch := pubsub.Channel()

	fmt.Println("Redis Event Bus listening...")
	for msg := range ch{
		fmt.Printf("BROADCAST: %s\n",msg.Payload)

		clientsMu.Lock()
		for client := range clients{
			err := client.WriteMessage(websocket.TextMessage, []byte(msg.Payload))
			if err != nil {
				log.Printf("Message didn't sent! Client will be deleted: %v\n",err)
				client.Close()
				delete(clients,client)
			}
		}
		clientsMu.Unlock()
	}
}

func handleWebSocket(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w,r,nil)
	if err != nil {
		log.Println("WebSocket Upgrade error:", err)
		return
	}

	clientsMu.Lock()
	clients[conn] = true
	clientsMu.Unlock()
	fmt.Println("New user joined the broadcast!")

	defer func() {
		clientsMu.Lock()
		delete(clients,conn)
		clientsMu.Unlock()
		conn.Close()
		fmt.Println("A user has left the broadcast!")
	}()

	for {
		_, message, err := conn.ReadMessage()
		if err != nil {
			log.Println("Reading error: ",err)
			break
		}

		var event models.BaseEvent
		if err := json.Unmarshal(message, &event); err != nil {
			log.Println("Invalid JSON format!")
			continue
		}

		if event.Type == models.EventBidPlaced{
			payloadBytes, _ := json.Marshal(event.Payload)
			var bidData models.BidPayload
			json.Unmarshal(payloadBytes, &bidData)

			fmt.Printf("New bid: %s -> %.2f TL\n", bidData.UserID, bidData.Amount)

			err := biddingService.ProcessBid(repository.Ctx, bidData)
			if err != nil {
				errorMsg := map[string]string{"type": "error", "message": err.Error()}
				conn.WriteJSON(errorMsg)
			}
		}
	}
}

func seedRedisData() {
	auctionID := "item101"

	auction := models.Auction{
		ID: auctionID,
		ProductName: "Antique Clock",
		StartingPrice: 100.0,
		CurrentPrice: 100.0,
		IsActive: true,
		EndTime: time.Now().Add(30 * time.Second),
	}

	data, _ := json.Marshal(auction)

	err :=rdb.Set(repository.Ctx, "auction:"+auctionID, data, 0).Err()
	if err != nil {
		log.Fatal("Seed error: ",err)
	}

	fmt.Println("Test data uploaded to Redis!")
}

func main() {
	rdb = repository.NewRedisClient()
	var err error
	pgDB, err = repository.NewPostgresDB()
	if err != nil {
		log.Fatalf("Postgres error: %v", err)
	}
	pgDB.InitTables()

	biddingService = service.NewBiddingService(rdb)
	seedRedisData()

	go subscribeToAuctionEvents()
	go checkAuctions()

	router := gin.Default()
	router.Use(func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})

	apiHandler := api.NewHandler(pgDB, rdb)

	router.POST("/register", apiHandler.Register)
	router.POST("/login", apiHandler.Login)
	router.POST("/auctions", apiHandler.CreateAuction)
	router.GET("/auctions", apiHandler.ListAuctions)

	router.GET("/ws", ginWebSocketHandler)

	fmt.Println("🚀 Auctionary API listening on port 8080...")
	router.Run(":8080")
}
















