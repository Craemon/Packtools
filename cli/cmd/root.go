package cmd

import (
	"os"
	"packtools/client"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var (
	ServerURL string
	RawJSON   bool
	ApiClient *client.Client
)

func GetClient() *client.Client {
	if ApiClient == nil {
		url := viper.GetString("url")
		if url == "" {
			url = "http://localhost:9501/api/v1"
		}
		ApiClient = client.NewClient(url)
	}
	return ApiClient
}

var rootCmd = &cobra.Command{
	Use:   "packtools",
	Short: "CLI tool for building, updating, and testing Minecraft packs",
	PersistentPreRun: func(cmd *cobra.Command, args []string) {
		GetClient()
	},
}

func Execute() {
	if err := rootCmd.Execute(); err != nil {
		os.Exit(1)
	}
}

func init() {
	rootCmd.PersistentFlags().StringVarP(&ServerURL, "url", "u", "http://localhost:9501/api/v1", "Ktor API base URL")
	rootCmd.PersistentFlags().BoolVar(&RawJSON, "json", false, "Output raw JSON response")

	_ = viper.BindPFlag("url", rootCmd.PersistentFlags().Lookup("url"))
	viper.SetEnvPrefix("PACKTOOLS")
	viper.AutomaticEnv()
}
