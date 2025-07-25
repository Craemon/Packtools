package com.craemon.packtools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PackLoader {

    public static List<Pack> loadAllPacks(Config config) {
        List<Pack> result = new ArrayList<>();

        loadByType(result, Pack.Type.DATAPACK, config.datapackRoot, config.datapackSubcategories);
        loadByType(result, Pack.Type.RESOURCEPACK, config.resourcepackRoot, config.resourcepackSubcategories);

        return result;
    }

    private static void loadByType(List<Pack> result, Pack.Type type, String rootPath, Map<String, List<String>> subcategories) {
        if (rootPath == null || subcategories == null) return;

        File root = new File(rootPath);

        for (Map.Entry<String, List<String>> entry : subcategories.entrySet()) {
            String category = entry.getKey();
            List<String> commons = entry.getValue();
            File categoryDir = new File(root, category);

            if (!categoryDir.isDirectory()) continue;

            File[] packs = categoryDir.listFiles(File::isDirectory);
            if (packs == null) continue;

            for (File pack : packs) {
                List<File> commonFolders = commons.stream()
                        .map(common -> new File(root, common))
                        .collect(Collectors.toList());

                result.add(new Pack(type, pack.getName(), category, pack, commonFolders));
            }
        }
    }
}
