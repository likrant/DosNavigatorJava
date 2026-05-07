package org.dosnavigator.panels;

import java.nio.file.Path;

public record FileItem(Path path, boolean directory, long size) {
    public String name() {
        Path name = path.getFileName();
        return name == null ? path.toString() : name.toString();
    }

    public String displayName() {
        return directory ? "[" + name() + "]" : name();
    }
}
