package org.dosnavigator.fs;

import java.nio.file.Path;

public record Drive(String name, Path root, long freeSpace) {
    public String label() {
        return name == null || name.isBlank() ? root.toString() : name;
    }
}
