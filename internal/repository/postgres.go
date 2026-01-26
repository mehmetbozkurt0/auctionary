package repository

import (
	"database/sql"
	"fmt"
	"log"
	"time"

	_"github.com/lib/pq"
	"github.com/mehmetbozkurt0/auctionary/internal/models"
)

type PostgresDB struct {
	DB *sql.DB
}

func NewPostgresDB() (*PostgresDB, error) {
	connStr := "postgres://user:password@localhost:5432/auctionary_db?sslmode=disable"

	db, err := sql.Open("postgres", connStr)
	if err != nil {
		return nil, err
	}

	if err := db.Ping(); err != nil {
		return nil, fmt.Errorf("Cannot connect postgres: %v", err)
	}

	return &PostgresDB{DB: db}, nil
}

func (p *PostgresDB) InitTables() {
	userQuery := `
		CREATE TABLE IF NOT EXISTS users (
		    id SERIAL PRIMARY KEY,
		    username VARCHAR(50) UNIQUE NOT NULL,
		    email VARCHAR(100) UNIQUE NOT NULL,
		    password VARCHAR(255) NOT NULL,
		    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
		);
	`
	if _, err := p.DB.Exec(userQuery); err != nil {
		log.Fatalf("User table create error: %v",err)
	}

	auctionQuery := `
		CREATE TABLE IF NOT EXISTS auctions (
		    id VARCHAR(50) PRIMARY KEY,
		    product_name VARCHAR(100),
		    starting_price DECIMAL(10,2),
		    final_price DECIMAL(10,2),
		    winner_id VARCHAR(50),
		    end_time TIMESTAMP,
		    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
		);  
	`

	_, err := p.DB.Exec(auctionQuery)
	if err != nil {
		log.Fatalf("Auction table create error: %v",err)
	}
	log.Println("Database tables ready++")
}

func (p *PostgresDB) CreateUser(user *models.User) error {
	query := `INSERT INTO users (username, email, password) VALUES ($1, $2, $3) RETURNING id`
	return p.DB.QueryRow(query, user.Username, user.Email, user.Password).Scan(&user.ID)
}

func (p *PostgresDB) GetUserByEmail(email string) (*models.User, error) {
	user := &models.User{}
	query := `SELECT id, username, email, password FROM users WHERE email = $1`

	err := p.DB.QueryRow(query, email).Scan(&user.ID, &user.Username, &user.Email, &user.Password)

	if err != nil {
		return nil, err
	}
	return user, nil
}

func (p *PostgresDB) SaveFinishedAuction(auction *models.Auction) error {
	query := `
		INSERT INTO auctions (id, product_name, starting_price, final_price, winner_id, end_time)
		VALUES ($1, $2, $3, $4, $5, $6)
		ON CONFLICT (id) DO UPDATE 
		SET final_price = EXCLUDED.final_price, winner_id = EXCLUDED.winner_id, end_time = EXCLUDED.end_time;
	`
	_, err := p.DB.Exec(query, auction.ID, auction.ProductName, auction.StartingPrice, auction.CurrentPrice, auction.WinnerID, auction.EndTime)
	return err
}


func (p *PostgresDB) CreateNewAuction(auction *models.Auction) error {
	query := `
		INSERT INTO auctions (id, product_name, starting_price, final_price, winner_id, end_time)
		VALUES ($1, $2, $3, $4, $5, $6)
	`
	_, err := p.DB.Exec(query, auction.ID, auction.ProductName, auction.StartingPrice, auction.StartingPrice, "", auction.EndTime)
	return err
}

func (p *PostgresDB) GetAllAuctions() ([]models.Auction, error) {
	query := `SELECT id, product_name, starting_price, final_price, winner_id, end_time FROM auctions ORDER BY end_time DESC`

	rows, err := p.DB.Query(query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var auctions []models.Auction
	for rows.Next() {
		var a models.Auction
		var winnerID sql.NullString

		if err := rows.Scan(&a.ID, &a.ProductName, &a.StartingPrice, &a.CurrentPrice, &winnerID, &a.EndTime); err != nil {
			return nil, err
		}
		a.WinnerID = winnerID.String

		if time.Now().Before(a.EndTime) {
			a.IsActive = true
		} else {
			a.IsActive = false
		}

		auctions = append(auctions, a)
	}
	return auctions, nil
}












