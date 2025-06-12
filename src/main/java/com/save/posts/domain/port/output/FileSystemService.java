package com.save.posts.domain.port.output;

import java.io.IOException;
import java.nio.file.Path;

public interface FileSystemService {
    void createDirectoriesIfNotExists(Path directoryPath) throws IOException;

    void writeStringToFile(Path filePath, String content) throws IOException;

    boolean exists(Path path);
}
