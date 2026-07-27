package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List server resources (packs, builds, artifacts)",
}

var listPacksCmd = &cobra.Command{
	Use:   "packs",
	Short: "List all registered packs",
	RunE: func(cmd *cobra.Command, args []string) error {
		resp, err := GetClient().Get("/packs")
		if err != nil {
			return err
		}
		fmt.Println(string(resp))
		return nil
	},
}

var listBuildsCmd = &cobra.Command{
	Use:   "builds",
	Short: "List all build run IDs",
	RunE: func(cmd *cobra.Command, args []string) error {
		resp, err := GetClient().Get("/builds")
		if err != nil {
			return err
		}
		fmt.Println(string(resp))
		return nil
	},
}

var listArtifactsCmd = &cobra.Command{
	Use:   "artifacts",
	Short: "List all generated artifacts",
	RunE: func(cmd *cobra.Command, args []string) error {
		resp, err := GetClient().Get("/artifacts")
		if err != nil {
			return err
		}
		fmt.Println(string(resp))
		return nil
	},
}

func init() {
	listCmd.AddCommand(listPacksCmd, listBuildsCmd, listArtifactsCmd)
	rootCmd.AddCommand(listCmd)
}
