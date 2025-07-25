package com.craemon.packtools.commands;

import com.craemon.packtools.Config;
import com.craemon.packtools.Main;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.*;

@CommandLine.Command(name = "clean", description = "Clean output directories for built packs.")
public class CleanCommand implements Runnable {

    @CommandLine.ParentCommand
    private Main main;

    @Override
    public void run() {
        Config config = main.getConfig();
        Path datapackOut = Paths.get(config.datapackRoot, "output");
        Path resourcepackOut = Paths.get(config.resourcepackRoot, "output");

        try {
            deleteDirectoryContents(datapackOut);
            deleteDirectoryContents(resourcepackOut);
            System.out.println("Cleaned output directories.");
        } catch (IOException e) {
            System.err.println("Failed to clean output directories: " + e.getMessage());
        }
    }

    private void deleteDirectoryContents(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete children first
                    .filter(p -> !p.equals(dir))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            System.err.println("Failed to delete " + p + ": " + e.getMessage());
                        }
                    });
        }
    }
}
