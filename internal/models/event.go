package models

type EventType string

const (
	EventBidPlaced   EventType = "bid_placed"
	EventBidAccepted EventType = "bid_accepted"
	EventAuctionEnd  EventType = "auction_end"

)

type BaseEvent struct {
	Type EventType `json:"type"`
	Payload interface{} `json:"payload"`

}

type BidPayload struct {
	AuctionID string `json:"auction_id"`
	UserID string `json:"user_id"`
	Amount float64 `json:"amount"`
}

type AuctionUpdatePayload struct {
	AuctionID string `json:"auction_id"`
	NewPrice float64 `json:"new_price"`
	WinnerID string `json:"winner_id"`
	RemainingTime int `json:"remaining_time_seconds"`
}
