package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

var buildCmd = &cobra.Command{
	Use:   "build <pack-id>",
	Short: "Trigger a build for a specific atomic or list pack",
	Args:  cobra.ExactArgs(1),
	// Dynamic TAB completion stuff for the pack IDs
	ValidArgsFunction: func(cmd *cobra.Command, args []string, toComplete string) ([]string, cobra.ShellCompDirective) {
		if len(args) != 0 {
			return nil, cobra.ShellCompDirectiveNoFileComp
		}
		ids, err := GetClient().FetchPackIDs()
		if err != nil {
			return nil, cobra.ShellCompDirectiveError
		}
		return ids, cobra.ShellCompDirectiveNoFileComp
	},
	RunE: func(cmd *cobra.Command, args []string) error {
		packID := args[0]
		resp, err := ApiClient.Post("/packs/" + packID + "/build")
		if err != nil {
			return err
		}

		fmt.Println(string(resp))
		return nil
	},
}

func init() {
	rootCmd.AddCommand(buildCmd)
}
