package org.dosnavigator.panels;

import org.dosnavigator.fs.FileRecord;
import org.dosnavigator.fs.LocalFileSystemService;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilePanelTest {
    @TempDir
    Path tempDir;

    @Test
    void includesParentEntryAndNavigatesUp() throws IOException {
        Path child = Files.createDirectory(tempDir.resolve("child"));

        FilePanel panel = new FilePanel(new LocalFileSystemService(), child);

        assertEquals("..", panel.records().getFirst().name());

        panel.handleKey(KeyStroke.of(KeyType.Enter));

        assertEquals(tempDir.toAbsolutePath().normalize(), panel.directory());
    }

    @Test
    void sortsByExtensionAndKeepsDirectoriesFirst() throws IOException {
        Files.createFile(tempDir.resolve("zeta.txt"));
        Files.createFile(tempDir.resolve("alpha.bin"));
        Files.createDirectory(tempDir.resolve("docs"));

        FilePanel panel = new FilePanel(new LocalFileSystemService(), tempDir);
        panel.setSortMode(PanelSortMode.EXTENSION);

        List<String> names = panel.records().stream()
                .filter(record -> !record.parentEntry())
                .map(FileRecord::name)
                .toList();

        assertEquals(List.of("docs", "alpha.bin", "zeta.txt"), names);
    }

    @Test
    void selectsAndInvertsFilesByMask() throws IOException {
        Files.writeString(tempDir.resolve("one.txt"), "123");
        Files.writeString(tempDir.resolve("two.log"), "12345");
        Files.createDirectory(tempDir.resolve("folder"));

        FilePanel panel = new FilePanel(new LocalFileSystemService(), tempDir);

        panel.selectByMask("*.txt");
        assertEquals(1, panel.selectedCount());
        assertEquals(3, panel.selectedSize());

        panel.invertSelection();
        assertEquals(1, panel.selectedCount());
        assertEquals(5, panel.selectedSize());
    }

    @Test
    void insertCanSelectDirectoryButPlainInvertSkipsDirectories() throws IOException {
        Files.createDirectory(tempDir.resolve("folder"));
        Files.createFile(tempDir.resolve("file.txt"));

        FilePanel panel = new FilePanel(new LocalFileSystemService(), tempDir);
        panel.handleKey(KeyStroke.character('f'));
        panel.handleKey(KeyStroke.of(KeyType.Insert));

        assertEquals(1, panel.selectedCount());

        panel.invertSelection();

        assertEquals(2, panel.selectedCount());
        assertEquals(0, panel.selectedSize());
    }

    @Test
    void sortPopupChangesSortMode() throws IOException {
        Files.createFile(tempDir.resolve("zeta.txt"));
        Files.createFile(tempDir.resolve("alpha.bin"));

        FilePanel panel = new FilePanel(new LocalFileSystemService(), tempDir);

        panel.handleKey(KeyStroke.of(KeyType.F9));
        panel.handleKey(KeyStroke.of(KeyType.ArrowDown));
        panel.handleKey(KeyStroke.of(KeyType.Enter));

        assertEquals(PanelSortMode.EXTENSION, panel.sortMode());
    }

    @Test
    void quickSearchAccumulatesCharacters() throws IOException {
        Files.createFile(tempDir.resolve("alpha.txt"));
        Files.createFile(tempDir.resolve("alpine.txt"));
        Files.createFile(tempDir.resolve("beta.txt"));

        FilePanel panel = new FilePanel(new LocalFileSystemService(), tempDir);

        panel.handleKey(KeyStroke.character('b'));

        assertTrue(panel.records().get(panel.selectedIndex()).name().startsWith("beta"));
    }
}
