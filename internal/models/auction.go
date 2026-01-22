package models

import "time"

type Auction struct {
	ID            string    `json:"id"`
	ProductName   string    `json:"product_name"`
	StartingPrice float64   `json:"starting_price"`
	CurrentPrice  float64   `json:"current_price"`
	WinnerID      string    `json:"winner_id"`
	EndTime       time.Time `json:"end_time"`
	IsActive      bool      `json:"is_active"`
}
type Bid struct {
	AuctionID string    `json:"auction_id"`
	UserID    string    `json:"user_id"`
	Amount    float64   `json:"amount"`
	CreatedAt time.Time `json:"created_at"`
}
