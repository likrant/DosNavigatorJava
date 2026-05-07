package org.dosnavigator.fs;

import org.dosnavigator.panels.FileItem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileSystemService {
    List<FileItem> list(Path directory) throws IOException;
}
