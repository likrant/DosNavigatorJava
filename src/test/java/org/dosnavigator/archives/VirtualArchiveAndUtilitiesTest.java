package org.dosnavigator.archives;

import org.dosnavigator.search.FileSearch;
import org.dosnavigator.tools.LegacyUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VirtualArchiveAndUtilitiesTest {
    @TempDir
    Path fixture;

    @Test
    void archiveSearchCalculatorAndCalendarUseOnlyDisposableReadOnlyData() throws Exception {
        Path archive = fixture.resolve("sample.zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("docs/"));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("docs/readme.txt"));
            zip.write("DN".getBytes());
            zip.closeEntry();
        }
        Files.createDirectory(fixture.resolve("tree"));
        Files.writeString(fixture.resolve("tree/needle.txt"), "x");

        assertEquals(List.of("docs/", "docs/readme.txt"), VirtualArchive.listZip(archive).stream().map(VirtualArchive.Entry::name).toList());
        assertEquals(List.of(fixture.resolve("tree/needle.txt")), FileSearch.byName(fixture, "needle"));
        assertEquals(42, LegacyUtilities.calculate(6, '*', 7));
        assertEquals(29, LegacyUtilities.daysInMonth(2024, 2));
    }
}
