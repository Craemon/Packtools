package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

var cleanCmd = &cobra.Command{
	Use:   "clean",
	Short: "Clean up server resources (builds, artifacts)",
}

var cleanBuildsCmd = &cobra.Command{
	Use:   "builds",
	Short: "Wipe build history directory",
	RunE: func(cmd *cobra.Command, args []string) error {
		resp, err := GetClient().Delete("/builds")
		if err != nil {
			return err
		}
		fmt.Println(string(resp))
		return nil
	},
}

var cleanArtifactsCmd = &cobra.Command{
	Use:   "artifacts",
	Short: "Wipe artifacts directory",
	RunE: func(cmd *cobra.Command, args []string) error {
		resp, err := GetClient().Delete("/artifacts")
		if err != nil {
			return err
		}
		fmt.Println(string(resp))
		return nil
	},
}

func init() {
	cleanCmd.AddCommand(cleanBuildsCmd, cleanArtifactsCmd)
	rootCmd.AddCommand(cleanCmd)
}
