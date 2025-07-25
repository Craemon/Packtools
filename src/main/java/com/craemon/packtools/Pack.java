package com.craemon.packtools;

import java.io.File;
import java.util.List;

public class Pack {
    public enum Type { DATAPACK, RESOURCEPACK }

    private final Type type;
    private final String name;
    private final String category;
    private final File path;
    private final List<File> commonFolders;

    public Pack(Type type, String name, String category, File path, List<File> commonFolders) {
        this.type = type;
        this.name = name;
        this.category = category;
        this.path = path;
        this.commonFolders = commonFolders;
    }

    public Type getType() { return type; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public File getPath() { return path; }
    public List<File> getCommonFolders() { return commonFolders; }

    @Override
    public String toString() {
        return String.format("%s [%s] in category '%s'", name, type, category);
    }
}

