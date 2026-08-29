package org.dosnavigator.archives;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipFile;

/** Read-only virtual-panel adapter for ARCHIVER.PAS and ARCVIEW.PAS. */
public final class VirtualArchive {
    private VirtualArchive() {
    }

    public static List<Entry> listZip(Path archive) throws IOException {
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            return zip.stream()
                    .map(entry -> new Entry(entry.getName(), entry.isDirectory(), entry.getSize()))
                    .sorted(Comparator.comparing(Entry::name))
                    .toList();
        }
    }

    public record Entry(String name, boolean directory, long size) {
    }
}
