package com.craemon.packtools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

public class Config {
    public String minecraftVersion;
    public String datapackVersion;
    public String resourcepackVersion;
    public String projectRoot;
    public String datapackRoot;
    public String resourcepackRoot;
    public String prismPath;
    public Map<String, List<String>> datapackSubcategories;
    public Map<String, List<String>> resourcepackSubcategories;

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static Config loadOrCreateDefault(Path path) throws IOException {
        if (Files.notExists(path)) {
            // Create parent dirs if needed
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Config defaultConfig = defaultConfig();
            defaultConfig.save(path);
            return defaultConfig;
        }
        return load(path);
    }

    public static Config load(Path jsonPath) throws IOException {
        return mapper.readValue(jsonPath.toFile(), Config.class);
    }

    public void save(Path jsonPath) throws IOException {
        mapper.writeValue(jsonPath.toFile(), this);
    }

    public static Config defaultConfig() {
        Config cfg = new Config();

        // Version metadata
        cfg.minecraftVersion = "1.21.8";    // Minecraft version for title and Test Instance
        cfg.datapackVersion = "81";         // Datapack version
        cfg.resourcepackVersion = "64";     // Resourcepack version

        // Path to your local project directory (this is where the program will operate)
        cfg.projectRoot = "/path/to/your/project/root";

        // Root directories for your datapack and resourcepack folders
        cfg.datapackRoot = "/path/to/your/project/root/Datapacks";
        cfg.resourcepackRoot = "/path/to/your/project/root/Resourcepacks";

        // Prism Launcher path (this is where your Test instance will be created)
        cfg.prismPath = "/path/to/PrismLauncher/";

        // Categories: I have left mine for reference, you will want to change yours (this defines which packs share which common folders)
        cfg.datapackSubcategories = Map.of(
                "crafting-packs", List.of("common", "crafting-common"),
                "packs", List.of("common")
        );
        cfg.resourcepackSubcategories = Map.of(
                "packs", List.of("common")
        );

        return cfg;
    }
}
