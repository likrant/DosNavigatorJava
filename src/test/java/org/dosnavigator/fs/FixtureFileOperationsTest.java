package org.dosnavigator.fs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixtureFileOperationsTest {
    @TempDir
    Path fixture;

    @Test
    void copyMoveRenameMakeDirectoryDeleteAndCancelStayInsideFixture() throws IOException {
        Files.writeString(fixture.resolve("source.txt"), "source");
        FixtureFileOperations operations = new FixtureFileOperations(fixture);

        operations.makeDirectory(Path.of("work"));
        operations.copy(Path.of("source.txt"), Path.of("work/copy.txt"), FixtureFileOperations.ConflictResolution.FAIL);
        Path moved = operations.move(Path.of("work/copy.txt"), Path.of("work/moved.txt"), FixtureFileOperations.ConflictResolution.FAIL);
        Path renamed = operations.rename(moved, "renamed.txt", FixtureFileOperations.ConflictResolution.FAIL);

        assertEquals("source", Files.readString(renamed));
        assertFalse(operations.delete(renamed, true));
        assertTrue(Files.exists(renamed));
        assertTrue(operations.delete(Path.of("work"), false));
        assertFalse(Files.exists(fixture.resolve("work")));
    }

    @Test
    void conflictAndTraversalAreExplicitlyRejected() throws IOException {
        Files.writeString(fixture.resolve("one.txt"), "one");
        Files.writeString(fixture.resolve("two.txt"), "two");
        FixtureFileOperations operations = new FixtureFileOperations(fixture);

        assertThrows(IOException.class, () -> operations.copy(Path.of("one.txt"), Path.of("two.txt"), FixtureFileOperations.ConflictResolution.FAIL));
        assertEquals(fixture.resolve("two.txt").toRealPath(), operations.copy(Path.of("one.txt"), Path.of("two.txt"), FixtureFileOperations.ConflictResolution.CANCEL));
        assertEquals("two", Files.readString(fixture.resolve("two.txt")));
        assertThrows(IllegalArgumentException.class, () -> operations.delete(Path.of("../outside"), false));
        assertThrows(IllegalArgumentException.class, () -> operations.rename(Path.of("one.txt"), "nested/name", FixtureFileOperations.ConflictResolution.FAIL));
    }
}
