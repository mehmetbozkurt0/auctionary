package main

import (
	"fmt"
	"log"

	"github.com/mehmetbozkurt0/auctionary/internal/repository"
)

func main() {
	// Redis bağlantısını başlat
	rdb := repository.NewRedisClient()

	err := rdb.Ping(repository.Ctx).Err()
	if err != nil {
		log.Fatalf("Redis bağlantısı kurulamadı: %v", err)
	}

	fmt.Println("🚀 Auctionary Backend Başarıyla Başlatıldı!")
	fmt.Println("✅ Redis Bağlantısı Aktif.")
	auctionID := "item101"
	initialPrice := "500.0"

	err = rdb.Set(repository.Ctx, auctionID, initialPrice, 0).Err()
	if err != nil {
		log.Fatalf("Veri yazılamadı: %v",err)
	}
	fmt.Printf("Redis'e yazıldı --> ID: %s, Başlangıç fiyatı: %s TL\n",auctionID, initialPrice)

	val, err := rdb.Get(repository.Ctx, auctionID).Result()
	if err != nil {
		log.Fatalf("Veri okunamadı: %v",err)
	}
	fmt.Println("Redis'ten okunan güncel fiyat: %s",val)





}
