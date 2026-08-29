package org.dosnavigator.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Objects;

/**
 * Scoped adapter for filecopy.pas, eraser.pas, filelst.pas and filetype.pas.
 *
 * <p>The production application must supply an explicitly authorised root. Tests
 * use {@code @TempDir}; no operation is permitted to escape that root.</p>
 */
public final class FixtureFileOperations {
    private final Path root;

    public FixtureFileOperations(Path root) throws IOException {
        this.root = Objects.requireNonNull(root, "root").toRealPath();
    }

    public Path copy(Path source, Path target, ConflictResolution conflict) throws IOException {
        Path checkedSource = requireExisting(source);
        Path checkedTarget = requireTarget(target);
        if (Files.exists(checkedTarget) && conflict == ConflictResolution.CANCEL) {
            return checkedTarget;
        }
        if (Files.exists(checkedTarget) && conflict == ConflictResolution.FAIL) {
            throw new IOException("Target already exists: " + checkedTarget.getFileName());
        }
        if (Files.isDirectory(checkedSource)) {
            copyDirectory(checkedSource, checkedTarget, conflict);
        } else {
            Files.createDirectories(checkedTarget.getParent());
            Files.copy(checkedSource, checkedTarget, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
        return checkedTarget;
    }

    public Path move(Path source, Path target, ConflictResolution conflict) throws IOException {
        Path checkedSource = requireExisting(source);
        Path checkedTarget = requireTarget(target);
        if (Files.exists(checkedTarget) && conflict == ConflictResolution.CANCEL) {
            return checkedTarget;
        }
        if (Files.exists(checkedTarget) && conflict == ConflictResolution.FAIL) {
            throw new IOException("Target already exists: " + checkedTarget.getFileName());
        }
        Files.createDirectories(checkedTarget.getParent());
        return Files.move(checkedSource, checkedTarget, StandardCopyOption.REPLACE_EXISTING);
    }

    public Path rename(Path source, String name, ConflictResolution conflict) throws IOException {
        if (name.isBlank() || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("A DOS Navigator name must be a single path segment");
        }
        Path checkedSource = requireExisting(source);
        return move(checkedSource, checkedSource.resolveSibling(name), conflict);
    }

    public Path makeDirectory(Path directory) throws IOException {
        return Files.createDirectories(requireTarget(directory));
    }

    public boolean delete(Path path, boolean cancelled) throws IOException {
        if (cancelled) {
            return false;
        }
        Path checked = requireExisting(path);
        if (Files.isDirectory(checked)) {
            try (var paths = Files.walk(checked)) {
                paths.sorted(Comparator.reverseOrder()).forEach(this::deleteUnchecked);
            }
        } else {
            Files.delete(checked);
        }
        return true;
    }

    private void copyDirectory(Path source, Path target, ConflictResolution conflict) throws IOException {
        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path destination = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    copy(path, destination, conflict);
                }
            }
        }
    }

    private Path requireExisting(Path path) throws IOException {
        Path checked = requireTarget(path);
        if (!Files.exists(checked)) {
            throw new IOException("Path does not exist: " + checked.getFileName());
        }
        Path real = checked.toRealPath();
        if (!real.startsWith(root)) {
            throw new IllegalArgumentException("Path resolves outside the authorised fixture root");
        }
        return real;
    }

    private Path requireTarget(Path path) throws IOException {
        Path checked = root.resolve(path).normalize();
        if (!checked.startsWith(root) || checked.equals(root)) {
            throw new IllegalArgumentException("Path is outside the authorised fixture root");
        }
        Path existingParent = checked.getParent();
        while (existingParent != null && !Files.exists(existingParent)) {
            existingParent = existingParent.getParent();
        }
        if (existingParent == null || !existingParent.toRealPath().startsWith(root)) {
            throw new IllegalArgumentException("Target resolves outside the authorised fixture root");
        }
        return checked;
    }

    private void deleteUnchecked(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to delete fixture path", e);
        }
    }

    public enum ConflictResolution {
        REPLACE,
        FAIL,
        CANCEL
    }
}
