package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/mehmetbozkurt0/auctionary/internal/repository"
	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize: 1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
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
		messageType, p, err := conn.ReadMessage()
		if err != nil {
			log.Println("Connection lost: ",err)
			break
		}
		fmt.Printf("Incoming message: %s\n",p)
		if err := conn.WriteMessage(messageType, p); err != nil {
			log.Println("Writing error: ",err)
			break
		}
	}
}

func main() {
	rdb := repository.NewRedisClient()
	err := rdb.Ping(repository.Ctx).Err()
	if err != nil {
		log.Fatalf("Cannot connect to Redis: %v", err)
	}
	fmt.Println("Redis connection is active!")

	http.HandleFunc("/ws",handleWebSocket)

	fmt.Println("Auctionary Backend has started to listen port 8080")
	if err := http.ListenAndServe(":8080",nil); err != nil {
		log.Fatal("Server error: ",err)
	}
}
















