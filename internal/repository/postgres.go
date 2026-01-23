package repository

import (
	"database/sql"
	"fmt"
	"log"

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
	query := `
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

	_, err := p.DB.Exec(query)
	if err != nil {
		log.Fatalf("Table create error: %v",err)
	}
	log.Println("Database tables ready++")
}

func (p *PostgresDB) SaveFinishedAuction(auction models.Auction) error {
	query := `
		INSERT INTO auctions (id, product_name, starting_price, final_price, winner_id, end_time)
		VALUES ($1, $2, $3, $4, $5, $6)
		ON CONFLICT (id) DO UPDATE 
		SET final_price = EXCLUDED.final_price, winner_id = EXCLUDED.winner_id, end_time = EXCLUDED.end_time;
	`
	_, err := p.DB.Exec(query, auction.ID, auction.ProductName, auction.StartingPrice, auction.CurrentPrice, auction.WinnerID, auction.EndTime)
	return err
}












