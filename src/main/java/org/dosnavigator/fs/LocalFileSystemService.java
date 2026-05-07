package org.dosnavigator.fs;

import org.dosnavigator.panels.FileItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class LocalFileSystemService implements FileSystemService {
    private static final Comparator<FileItem> FILE_ORDER = Comparator
            .comparing(FileItem::directory).reversed()
            .thenComparing(item -> item.name().toLowerCase());

    @Override
    public List<FileItem> list(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .map(this::toItem)
                    .sorted(FILE_ORDER)
                    .toList();
        }
    }

    private FileItem toItem(Path path) {
        boolean directory = Files.isDirectory(path);
        long size = 0L;
        if (!directory) {
            try {
                size = Files.size(path);
            } catch (IOException ignored) {
                size = -1L;
            }
        }
        return new FileItem(path, directory, size);
    }
}
