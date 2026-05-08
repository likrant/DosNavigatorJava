package org.dosnavigator.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class LocalFileSystemService implements FileSystemService {
    @Override
    public DirectoryModel open(Path directory) throws IOException {
        Path normalized = directory.toAbsolutePath().normalize();
        List<FileRecord> records = new ArrayList<>();
        Path parent = normalized.getParent();
        if (parent != null) {
            records.add(FileRecord.parent(parent));
        }
        try (Stream<Path> paths = Files.list(normalized)) {
            paths
                    .map(this::toItem)
                    .forEach(records::add);
        }
        return new DirectoryModel(driveFor(normalized), normalized, records);
    }

    private FileRecord toItem(Path path) {
        try {
            BasicFileAttributes basic = Files.readAttributes(path, BasicFileAttributes.class);
            DosFileAttributes dos = readDosAttributes(path);
            boolean hidden = dos != null ? dos.isHidden() : Files.isHidden(path);
            boolean system = dos != null && dos.isSystem();
            long size = basic.isDirectory() ? 0L : basic.size();
            Path fileName = path.getFileName();
            String name = fileName == null ? path.toString() : fileName.toString();
            return new FileRecord(
                    path,
                    name,
                    false,
                    basic.isDirectory(),
                    hidden,
                    system,
                    size,
                    basic.lastModifiedTime(),
                    basic.creationTime(),
                    basic.lastAccessTime()
            );
        } catch (IOException e) {
            Path fileName = path.getFileName();
            String name = fileName == null ? path.toString() : fileName.toString();
            return new FileRecord(path, name, false, Files.isDirectory(path), false, false, -1L, null, null, null);
        }
    }

    private Drive driveFor(Path directory) throws IOException {
        Path root = directory.getRoot();
        if (root == null) {
            root = directory;
        }
        return new Drive(root.toString(), root, Files.getFileStore(directory).getUsableSpace());
    }

    private DosFileAttributes readDosAttributes(Path path) {
        try {
            return Files.readAttributes(path, DosFileAttributes.class);
        } catch (UnsupportedOperationException | IOException ignored) {
            return null;
        }
    }
}
