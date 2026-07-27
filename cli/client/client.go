package client

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

type Client struct {
	BaseURL    string
	HTTPClient *http.Client
}

type Pack struct {
	Type                string         `json:"type"`
	ID                  string         `json:"id"`
	Name                string         `json:"name"`
	SpecVersion         int            `json:"specVersion"`
	Author              string         `json:"author,omitempty"`
	Characteristics     []string       `json:"characteristics,omitempty"`
	ArtifactNamePattern string         `json:"artifactNamePattern,omitempty"`
	PreBuild            []string       `json:"preBuild,omitempty"`
	ChildPacks          []string       `json:"childPacks,omitempty"`
	Structure           map[string]any `json:"structure,omitempty"`
}

type ManifestItem struct {
	ID              string   `json:"id"`
	Version         string   `json:"version"`
	Path            string   `json:"path"`
	Characteristics []string `json:"characteristics"`
}

type StandardResponse struct {
	Status  string `json:"status,omitempty"`
	Message string `json:"message,omitempty"`
	Error   string `json:"error,omitempty"`
}

func NewClient(baseURL string) *Client {
	return &Client{
		BaseURL: baseURL,
		HTTPClient: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

func (c *Client) Get(endpoint string) ([]byte, error) {
	resp, err := c.HTTPClient.Get(c.BaseURL + endpoint)
	if err != nil {
		return nil, fmt.Errorf("server unreachable (%s): %w", c.BaseURL, err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if resp.StatusCode >= 400 {
		return nil, fmt.Errorf("HTTP %d: %s", resp.StatusCode, string(body))
	}
	return body, err
}

func (c *Client) Post(endpoint string) ([]byte, error) {
	resp, err := c.HTTPClient.Post(c.BaseURL+endpoint, "application/json", nil)
	if err != nil {
		return nil, fmt.Errorf("server unreachable (%s): %w", c.BaseURL, err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if resp.StatusCode >= 400 {
		return nil, fmt.Errorf("HTTP %d: %s", resp.StatusCode, string(body))
	}
	return body, err
}

func (c *Client) Delete(endpoint string) ([]byte, error) {
	req, err := http.NewRequest(http.MethodDelete, c.BaseURL+endpoint, nil)
	if err != nil {
		return nil, err
	}
	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("server unreachable (%s): %w", c.BaseURL, err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if resp.StatusCode >= 400 {
		return nil, fmt.Errorf("HTTP %d: %s", resp.StatusCode, string(body))
	}
	return body, err
}

// --- Autocomplete Helpers ---

// FetchPackIDs returns pack IDs for TAB autocompletion.
func (c *Client) FetchPackIDs() ([]string, error) {
	body, err := c.Get("/packs")
	if err != nil {
		return nil, err
	}
	var packs []Pack
	if err := json.Unmarshal(body, &packs); err != nil {
		return nil, err
	}
	var ids []string
	for _, p := range packs {
		ids = append(ids, p.ID)
	}
	return ids, nil
}

// FetchRunIDs returns build run IDs for TAB autocompletion.
func (c *Client) FetchRunIDs() ([]string, error) {
	body, err := c.Get("/builds")
	if err != nil {
		return nil, err
	}
	var runs []string
	if err := json.Unmarshal(body, &runs); err != nil {
		return nil, err
	}
	return runs, nil
}
