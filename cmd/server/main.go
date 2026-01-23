package main

import (
	"fmt"
	"log"
	"net/http"
	"encoding/json"
	"time"

	"github.com/mehmetbozkurt0/auctionary/internal/models"
	"github.com/mehmetbozkurt0/auctionary/internal/repository"
	"github.com/mehmetbozkurt0/auctionary/internal/service"
	"github.com/gorilla/websocket"
	"github.com/redis/go-redis/v9"
)

var (
	rdb *redis.Client
	biddingService *service.BiddingService
	upgrader = websocket.Upgrader{
		CheckOrigin: func(r *http.Request) bool {return true},
	}
)

func checkAuctions() {
	ticker := time.NewTicker(1*time.Second)
	for range ticker.C {
		val, err := rdb.Get(repository.Ctx, "auction:item101").Result()
		if err != nil {
			continue
		}

		var auction models.Auction
		json.Unmarshal([]byte(val), &auction)

		if !auction.IsActive {
			continue
		}

		if time.Now().After(auction.EndTime) {
			fmt.Printf("ITEM SOLD!! Winner: %s - %.2f TL", auction.WinnerID, auction.CurrentPrice)

			auction.IsActive = false
			updateData, _ := json.Marshal(auction)
			rdb.Set(repository.Ctx, "auction:item101", updateData, 0)

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

func subscribeToAuctionEvents() {
	pubsub := rdb.Subscribe(repository.Ctx, "auction_events")

	ch := pubsub.Channel()

	fmt.Println("Redis Event Bus listening...")
	for msg := range ch{
		fmt.Printf("Redis has an update: %s\n",msg.Payload)
	}
}

func handleWebSocket(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w,r,nil)
	if err != nil {
		log.Println("WebSocket Upgrade error:", err)
		return
	}
	defer conn.Close()

	fmt.Println("New bidder connected!")

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
				log.Println("Bid denied: ",err)
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
	err := rdb.Ping(repository.Ctx).Err()
	if err != nil {
		log.Fatalf("Cannot connect to Redis: %v", err)
	}
	fmt.Println("Redis connection is active!")

	biddingService = service.NewBiddingService(rdb)

	seedRedisData()

	go subscribeToAuctionEvents()
	go checkAuctions()

	http.HandleFunc("/ws",handleWebSocket)

	fmt.Println("Auctionary Backend has started to listen port 8080")
	if err := http.ListenAndServe(":8080",nil); err != nil {
		log.Fatal("Server error: ",err)
	}
}
















