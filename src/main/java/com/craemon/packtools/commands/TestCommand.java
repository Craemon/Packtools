package com.craemon.packtools.commands;

import com.craemon.packtools.Config;
import com.craemon.packtools.Main;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@CommandLine.Command(name = "test", description = "Create Prism Launcher test instance")
public class TestCommand implements Runnable {

    @CommandLine.ParentCommand
    private Main main;

    @Override
    public void run() {
        Config cfg = main.getConfig();
        String mcVersion = cfg.minecraftVersion;
        String instanceName = "AutoPackTest_" + mcVersion;
        String group = "Test";

        Path prism = Paths.get(cfg.prismPath);
        Path instGroupsPath = prism.resolve("instances/instgroups.json");
        Path instancePath = prism.resolve("instances").resolve(instanceName);
        Path minecraftPath = instancePath.resolve(".minecraft");
        Path savesPath = minecraftPath.resolve("saves/TestWorld");

        try {
            // Create instgroups.json or update
            Map<String, Object> groups = new HashMap<>();
            if (Files.exists(instGroupsPath)) {
                groups = new ObjectMapper().readValue(instGroupsPath.toFile(), Map.class);
            } else {
                groups.put("formatVersion", "1");
                groups.put("groups", new HashMap<>());
            }

            Map<String, Object> groupMap = (Map<String, Object>) groups.get("groups");
            groupMap.computeIfAbsent(group, k -> Map.of("hidden", false, "instances", new ArrayList<>()));
            List<String> instanceList = (List<String>) ((Map<String, Object>) groupMap.get(group)).get("instances");
            if (!instanceList.contains(instanceName)) {
                instanceList.add(instanceName);
            }

            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(instGroupsPath.toFile(), groups);

            // Make folder structure
            Files.createDirectories(savesPath);
            Files.createDirectories(minecraftPath.resolve("resourcepacks"));

            // Write instance.cfg
            String instanceCfg = String.format(
                    "InstanceType=OneSix\nname=%s\niconKey=minecraft\nlastLaunchTime=0\ntotalTimePlayed=0\nmcVersion=%s\n",
                    instanceName, mcVersion
            );
            Files.writeString(instancePath.resolve("instance.cfg"), instanceCfg);

            // Write mmc-pack.json
            String mmcJson = String.format("""
                {
                  "components": [
                    {
                      "important": true,
                      "uid": "net.minecraft",
                      "version": "%s"
                    }
                  ],
                  "formatVersion": 1
                }
                """, mcVersion);
            Files.writeString(instancePath.resolve("mmc-pack.json"), mmcJson);

            // Copy test world
            Path srcWorld = Paths.get(cfg.projectRoot, "PackTestWorld");
            if (Files.exists(srcWorld)) {
                copyDirectory(srcWorld, savesPath);
            }

            // Copy datapacks
            Path srcData = Paths.get(cfg.datapackRoot, "output");
            if (Files.exists(srcData)) {
                copyDirectory(srcData, savesPath.resolve("datapacks"));
            }

            // Copy resourcepacks
            Path srcRes = Paths.get(cfg.resourcepackRoot, "output");
            if (Files.exists(srcRes)) {
                copyDirectory(srcRes, minecraftPath.resolve("resourcepacks"));
            }

            // Copy options.txt
            Path options = Paths.get(cfg.projectRoot, "options.txt");
            if (Files.exists(options)) {
                Files.copy(options, minecraftPath.resolve("options.txt"), StandardCopyOption.REPLACE_EXISTING);
            }

            // Attempt to open Prism Launcher
            try {
                new ProcessBuilder("flatpak", "run", "org.prismlauncher.PrismLauncher").start();
                new ProcessBuilder("xdg-open", instancePath.toString()).start();
            } catch (IOException e) {
                System.out.println("Instance created. Open manually in Prism Launcher.");
            }

            System.out.println("Test instance created: " + instanceName);
        } catch (IOException e) {
            System.err.println("Failed to create instance: " + e.getMessage());
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path dest = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
