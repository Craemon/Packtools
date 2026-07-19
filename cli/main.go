package main

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"
)

// Define the exact structural match of the Kotlin 'Pack' object
type Pack struct {
	ID      string `json:"id"`
	Name    string `json:"name"`
	Version string `json:"version"`
	Status  string `json:"status"`
}

const apiURL = "http://localhost:8080/api/v1/packs"

func main() {
	fmt.Println("🚀 Packtools CLI connecting to Core...")

	// 1. Setup an HTTP client with a reasonable timeout
	client := http.Client{
		Timeout: 5 * time.Second,
	}

	// 2. Fire the GET request to the Kotlin backend
	resp, err := client.Get(apiURL)
	if err != nil {
		fmt.Printf("❌ Error connecting to Core backend: %v\n", err)
		os.Exit(1)
	}
	defer resp.Body.Close() // Ensure we clean up network resources when done

	// 3. Handle non-200 responses safely
	if resp.StatusCode != http.StatusOK {
		fmt.Printf("❌ Core returned unexpected status: %s\n", resp.Status)
		os.Exit(1)
	}

	// 4. Read the raw JSON data body
	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("❌ Failed to read data stream: %v\n", err)
		os.Exit(1)
	}

	// 5. Unmarshal (parse) the JSON array into our Go slice
	var packs []Pack
	if err := json.Unmarshal(bodyBytes, &packs); err != nil {
		fmt.Printf("❌ Failed to parse JSON data: %v\n", err)
		os.Exit(1)
	}

	// 6. Print out the results beautifully
	fmt.Printf("\n📦 Available Packs Found (%d):\n", len(packs))
	fmt.Println("--------------------------------------------")
	for _, p := range packs {
		fmt.Printf("- [%s] %s (v%s) -> Status: %s\n", p.ID, p.Name, p.Version, p.Status)
	}
	fmt.Println("--------------------------------------------")
}
