package com.save.posts.infrastructure.adapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.save.posts.domain.port.output.FileSystemService;


@Component
public class NioFileSystemAdapter implements FileSystemService {

    @Override
    public void createDirectoriesIfNotExists(Path directoryPath) throws IOException {
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
    }

    @Override
    public void writeStringToFile(Path filePath, String content) throws IOException {
        Files.writeString(filePath, content);
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }
}
