package org.dosnavigator.fs;

import java.io.IOException;
import java.nio.file.Path;

public interface FileSystemService {
    DirectoryModel open(Path directory) throws IOException;
}
