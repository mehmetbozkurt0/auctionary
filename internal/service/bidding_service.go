package service

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"time"

	"github.com/mehmetbozkurt0/auctionary/internal/models"
	"github.com/mehmetbozkurt0/auctionary/internal/repository"
	"github.com/redis/go-redis/v9"
)

type BiddingService struct {
	RedisClient *redis.Client
}

func NewBiddingService(rdb *redis.Client) *BiddingService {
	return &BiddingService{RedisClient: rdb}
}

func (s *BiddingService) ProcessBid(ctx context.Context, bid models.BidPayload) error {
	auctionKey := fmt.Sprintf("auction:%s",bid.AuctionID)

	val, err := s.RedisClient.Get(ctx, auctionKey).Result()
	if err == redis.Nil {
		return errors.New("There's not an auction with this id!")
	}else if err != nil {
		return err
	}

	var auction models.Auction
	if err := json.Unmarshal([]byte(val), &auction); err != nil {
		return fmt.Errorf("Auction data is corrupted: %v",err)
	}

	if !auction.IsActive {
		return errors.New("Selected auction is completed!")
	}

	if bid.Amount <= auction.CurrentPrice {
		return fmt.Errorf("Bid isn't high enough! Current price is: %.2f",auction.CurrentPrice)
	}

	auction.CurrentPrice = bid.Amount
	auction.WinnerID = bid.UserID
	auction.EndTime = time.Now().Add(15*time.Second)

	updatedData, _ := json.Marshal(auction)
	if err := s.RedisClient.Set(ctx, auctionKey, updatedData, 0).Err(); err != nil {
		return err
	}

	log.Printf("Bid taken: %s - %.2f",bid.UserID, bid.Amount)

	remaining := time.Until(auction.EndTime).Seconds()

	updateEvent := models.BaseEvent{
		Type: models.EventBidAccepted,
		Payload: models.AuctionUpdatePayload{
			AuctionID: auction.ID,
			NewPrice: auction.CurrentPrice,
			WinnerID: auction.WinnerID,
			RemainingTime: int(remaining),
		},
	}
	return repository.PublishEvent(s.RedisClient, "auction_events", updateEvent)

}








