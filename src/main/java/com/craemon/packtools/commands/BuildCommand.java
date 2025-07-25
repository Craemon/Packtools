package com.craemon.packtools.commands;

import com.craemon.packtools.Config;
import com.craemon.packtools.Main;
import com.craemon.packtools.Pack;
import com.craemon.packtools.PackLoader;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@CommandLine.Command(name = "build", description = "Build packs: all, by type, or by category")
public class BuildCommand implements Runnable {

    @CommandLine.ParentCommand
    private Main main;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Pack type to build: DATAPACK or RESOURCEPACK")
    private String typeFilter;

    @CommandLine.Option(names = {"-c", "--category"}, description = "Category to build")
    private String categoryFilter;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Specific pack name to build")
    private String nameFilter;

    @Override
    public void run() {
        Config config = main.getConfig();
        List<Pack> allPacks = PackLoader.loadAllPacks(config);

        List<Pack> toBuild = allPacks;

        if (typeFilter != null) {
            Pack.Type type;
            try {
                type = Pack.Type.valueOf(typeFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid type: " + typeFilter);
                return;
            }
            toBuild = toBuild.stream()
                    .filter(p -> p.getType() == type)
                    .collect(Collectors.toList());
        }

        if (categoryFilter != null) {
            toBuild = toBuild.stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(categoryFilter))
                    .collect(Collectors.toList());
        }

        if (nameFilter != null) {
            toBuild = toBuild.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(nameFilter))
                    .collect(Collectors.toList());
        }

        if (toBuild.isEmpty()) {
            System.out.println("No packs to build with given filters.");
            return;
        }

        for (Pack pack : toBuild) {
            try {
                build(pack, config);
            } catch (IOException e) {
                System.err.printf("Failed to build pack %s: %s\n", pack.getName(), e.getMessage());
            }
        }
    }

    private void build(Pack pack, Config config) throws IOException {
        Path tempDir = Files.createTempDirectory("packtools_build_");

        // Copy common folders (contents only)
        for (File common : pack.getCommonFolders()) {
            copyContents(common.toPath(), tempDir);
        }

        // Copy pack folder (contents only)
        copyContents(pack.getPath().toPath(), tempDir);

        // Output directory and zip name
        Path outputDir = getOutputDirectory(pack, config);
        Files.createDirectories(outputDir);

        String zipName = getVersionedZipName(pack, config);
        Path zipPath = outputDir.resolve(zipName);

        // Zip it
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            zipFolderContents(tempDir, tempDir, zos);
        }

        System.out.printf("Built %s [%s] -> %s\n", pack.getName(), pack.getType(), zipPath);

        deleteRecursively(tempDir);
    }

    private void copyContents(Path sourceDir, Path targetDir) throws IOException {
        if (!Files.exists(sourceDir)) return;

        Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .forEach(source -> {
                    try {
                        Path relative = sourceDir.relativize(source);
                        Path target = targetDir.resolve(relative);
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private String getVersionedZipName(Pack pack, Config config) {
        String version = switch (pack.getType()) {
            case DATAPACK -> config.minecraftVersion + "-" + config.datapackVersion;
            case RESOURCEPACK -> config.minecraftVersion + "-" + config.resourcepackVersion;
        };
        return pack.getName() + "-" + version + ".zip";
    }

    private Path getOutputDirectory(Pack pack, Config config) {
        return pack.getType() == Pack.Type.DATAPACK
                ? Paths.get(config.datapackRoot, "output")
                : Paths.get(config.resourcepackRoot, "output");
    }

    private void zipFolderContents(Path baseDir, Path current, ZipOutputStream zos) throws IOException {
        Files.walk(current)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        String entryName = baseDir.relativize(path).toString().replace("\\", "/");
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) return;
        Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)) // delete children first
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        System.err.println("Failed to delete " + p + ": " + e.getMessage());
                    }
                });
    }
}
