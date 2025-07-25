package com.craemon.packtools;

import picocli.CommandLine;
import java.nio.file.Path;
import java.nio.file.Paths;

@CommandLine.Command(
        name = "packtools",
        mixinStandardHelpOptions = true,
        version = "packtools " + Main.VERSION,
        subcommands = {
                com.craemon.packtools.commands.ListCommand.class,
                com.craemon.packtools.commands.BuildCommand.class,
                com.craemon.packtools.commands.CleanCommand.class,
                com.craemon.packtools.commands.TestCommand.class,
                com.craemon.packtools.config.SetVersionCommand.class
        }
)
public class Main implements Runnable {

    public static final String VERSION = "1.0.0";

    // Make configPath optional, no 'required = true'
    @CommandLine.Option(names = {"-c", "--config"}, description = "Path to config JSON file")
    private Path configPath;

    private Config config;

    private static final Path DEFAULT_CONFIG_PATH =
            Paths.get(System.getProperty("user.home"), ".config", "packtools", "config.json");

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // If user did not provide -c, fallback to default path
        if (configPath == null) {
            configPath = DEFAULT_CONFIG_PATH;
        }

        try {
            // Load or create default config
            config = Config.loadOrCreateDefault(configPath);
            printSummary();
        } catch (Exception e) {
            System.err.println("Failed to load or create config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printSummary() {
        System.out.println("Minecraft Version: " + config.minecraftVersion);
        System.out.println("Datapack Version: " + config.datapackVersion);
        System.out.println("Resourcepack Version: " + config.resourcepackVersion);
        System.out.println("Datapack Root: " + config.datapackRoot);
        System.out.println("Resourcepack Root: " + config.resourcepackRoot);

        System.out.println("\nDatapack Categories:");
        if (config.datapackSubcategories != null && !config.datapackSubcategories.isEmpty()) {
            config.datapackSubcategories.keySet().forEach(cat -> System.out.println("  - " + cat));
        } else {
            System.out.println("  (none)");
        }

        System.out.println("\nResourcepack Categories:");
        if (config.resourcepackSubcategories != null && !config.resourcepackSubcategories.isEmpty()) {
            config.resourcepackSubcategories.keySet().forEach(cat -> System.out.println("  - " + cat));
        } else {
            System.out.println("  (none)");
        }
    }

    // For subcommands to get the loaded config
    public Config getConfig() {
        if (config == null) {
            try {
                if (configPath == null) {
                    configPath = DEFAULT_CONFIG_PATH;
                }
                config = Config.loadOrCreateDefault(configPath);
            } catch (Exception e) {
                throw new RuntimeException("Could not load config", e);
            }
        }
        return config;
    }
}
