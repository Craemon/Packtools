package com.craemon.packtools.config;

import com.craemon.packtools.Config;
import com.craemon.packtools.Main;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "setversion", description = "Set Minecraft, datapack, and resourcepack versions.")
public class SetVersionCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--minecraft", description = "Minecraft version")
    private String minecraft;

    @CommandLine.Option(names = "--datapack", description = "Datapack version")
    private String datapack;

    @CommandLine.Option(names = "--resourcepack", description = "Resourcepack version")
    private String resourcepack;

    @CommandLine.ParentCommand
    private Main main;

    @Override
    public Integer call() {
        Config config = main.getConfig();

        boolean interactive = (minecraft == null && datapack == null && resourcepack == null);

        if (interactive) {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Current Minecraft version: " + config.minecraftVersion);
                System.out.print("New Minecraft version (leave blank to keep current): ");
                String input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    config.minecraftVersion = input;
                }

                System.out.println("Current Datapack version: " + config.datapackVersion);
                System.out.print("New Datapack version (leave blank to keep current): ");
                input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    config.datapackVersion = input;
                }

                System.out.println("Current Resourcepack version: " + config.resourcepackVersion);
                System.out.print("New Resourcepack version (leave blank to keep current): ");
                input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    config.resourcepackVersion = input;
                }
            }
        } else {
            if (minecraft != null) config.minecraftVersion = minecraft;
            if (datapack != null) config.datapackVersion = datapack;
            if (resourcepack != null) config.resourcepackVersion = resourcepack;
        }

        try {
            Path configPath = Paths.get(System.getProperty("user.home"), ".config", "packtools", "config.json");
            config.save(configPath);
            System.out.println("Versions updated successfully.");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
            return 1;
        }

        return 0;
    }
}
