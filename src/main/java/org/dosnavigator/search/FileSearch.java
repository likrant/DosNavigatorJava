package org.dosnavigator.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Read-only traversal from the search vertical; callers define the search root. */
public final class FileSearch {
    private FileSearch() {
    }

    public static List<Path> byName(Path root, String fragment) throws IOException {
        String normalized = fragment.toLowerCase();
        try (var paths = Files.walk(root)) {
            return paths.filter(path -> !path.equals(root))
                    .filter(path -> path.getFileName().toString().toLowerCase().contains(normalized))
                    .sorted()
                    .toList();
        }
    }
}
