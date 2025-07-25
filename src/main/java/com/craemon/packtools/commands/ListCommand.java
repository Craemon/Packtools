package com.craemon.packtools.commands;

import com.craemon.packtools.Main;
import com.craemon.packtools.Pack;
import com.craemon.packtools.PackLoader;
import com.craemon.packtools.Config;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandLine.Command(name = "list", description = "List available packs by category and type")
public class ListCommand implements Runnable {

    @CommandLine.ParentCommand
    private Main main;

    @Override
    public void run() {
        Config config = main.getConfig();
        List<Pack> allPacks = PackLoader.loadAllPacks(config);

        Map<String, List<Pack>> grouped = allPacks.stream()
                .collect(Collectors.groupingBy(p -> p.getType() + " :: " + p.getCategory()));

        for (Map.Entry<String, List<Pack>> entry : grouped.entrySet()) {
            System.out.println("- " + entry.getKey() + ":");
            for (Pack pack : entry.getValue()) {
                System.out.println("    - " + pack.getName());
            }
        }
    }
}
