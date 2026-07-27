package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

var getCmd = &cobra.Command{
	Use:   "get",
	Short: "Get detailed information for a specific resource",
}

var getPackCmd = &cobra.Command{
	Use:   "pack <pack-id>",
	Short: "Get definition of a specific pack",
	Args:  cobra.ExactArgs(1),
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
		resp, err := GetClient().Get("/packs/" + args[0])
		if err != nil {
			return err
		}
		fmt.Println(string(resp))
		return nil
	},
}

var getManifestCmd = &cobra.Command{
	Use:   "manifest <run-id>",
	Short: "Get manifest for a specific build run",
	Args:  cobra.ExactArgs(1),
	ValidArgsFunction: func(cmd *cobra.Command, args []string, toComplete string) ([]string, cobra.ShellCompDirective) {
		if len(args) != 0 {
			return nil, cobra.ShellCompDirectiveNoFileComp
		}
		runs, err := GetClient().FetchRunIDs()
		if err != nil {
			return nil, cobra.ShellCompDirectiveError
		}
		return runs, cobra.ShellCompDirectiveNoFileComp
	},
	RunE: func(cmd *cobra.Command, args []string) error {
		resp, err := GetClient().Get("/builds/" + args[0] + "/manifest")
		if err != nil {
			return err
		}
		fmt.Println(string(resp))
		return nil
	},
}

func init() {
	getCmd.AddCommand(getPackCmd, getManifestCmd)
	rootCmd.AddCommand(getCmd)
}
