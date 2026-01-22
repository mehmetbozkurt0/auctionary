package repository

import (
	"context"

	"github.com/redis/go-redis/v9"
)

var Ctx = context.Background()

// NewRedisClient, Docker üzerinde çalışan Redis'e bağlanır [cite: 35]
func NewRedisClient() *redis.Client {
	return redis.NewClient(&redis.Options{
		Addr:     "localhost:6379", // Docker default portu
		Password: "",               // Şifre belirlemedik
		DB:       0,                // Default veritabanı
	})
}
