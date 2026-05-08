package org.dosnavigator.fs;

import java.nio.file.Path;
import java.util.List;

public record DirectoryModel(Drive drive, Path directory, List<FileRecord> records) {
    public DirectoryModel {
        records = List.copyOf(records);
    }

    public boolean atRoot() {
        return directory.getParent() == null;
    }
}
