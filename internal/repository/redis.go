package repository

import (
	"context"
	"encoding/json"

	"github.com/redis/go-redis/v9"
)

var Ctx = context.Background()


func NewRedisClient() *redis.Client {
	return redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "",
		DB:       0,
	})
}

func PublishEvent(rdb *redis.Client, channel string, payload interface{}) error {
	data, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	return rdb.Publish(Ctx, channel, data).Err()
}













