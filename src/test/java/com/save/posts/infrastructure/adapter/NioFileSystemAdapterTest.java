package com.save.posts.infrastructure.adapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NioFileSystemAdapterTest {

    private NioFileSystemAdapter fileSystemAdapter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileSystemAdapter = new NioFileSystemAdapter();
    }

    @Test
    void should_CreateDirectories_WhenNotExists() throws IOException {
        Path newDirectory = tempDir.resolve("new/nested/directory");
        assertFalse(Files.exists(newDirectory));

        fileSystemAdapter.createDirectoriesIfNotExists(newDirectory);

        assertTrue(Files.exists(newDirectory));
        assertTrue(Files.isDirectory(newDirectory));
    }

    @Test
    void should_NotCreateDirectories_WhenAlreadyExists() throws IOException {
        Path existingDirectory = tempDir.resolve("existing");
        Files.createDirectory(existingDirectory);
        assertTrue(Files.exists(existingDirectory));

        fileSystemAdapter.createDirectoriesIfNotExists(existingDirectory);

        assertTrue(Files.exists(existingDirectory));
        assertTrue(Files.isDirectory(existingDirectory));
    }

    @Test
    void should_WriteStringToFile_Successfully() throws IOException {
        Path filePath = tempDir.resolve("test.txt");
        String content = "Hello, World!";

        fileSystemAdapter.writeStringToFile(filePath, content);

        assertTrue(Files.exists(filePath));
        String actualContent = Files.readString(filePath);
        assertTrue(actualContent.equals(content));
    }

    @Test
    void should_OverwriteFile_WhenFileExists() throws IOException {
        Path filePath = tempDir.resolve("existing.txt");
        String originalContent = "Original content";
        String newContent = "New content";

        Files.writeString(filePath, originalContent);
        assertTrue(Files.exists(filePath));

        fileSystemAdapter.writeStringToFile(filePath, newContent);

        String actualContent = Files.readString(filePath);
        assertTrue(actualContent.equals(newContent));
    }

    @Test
    void should_WriteEmptyStringToFile() throws IOException {
        Path filePath = tempDir.resolve("empty.txt");
        String emptyContent = "";

        fileSystemAdapter.writeStringToFile(filePath, emptyContent);

        assertTrue(Files.exists(filePath));
        String actualContent = Files.readString(filePath);
        assertTrue(actualContent.equals(emptyContent));
    }

    @Test
    void should_ReturnTrue_WhenPathExists() throws IOException {
        Path existingFile = tempDir.resolve("existing.txt");
        Files.createFile(existingFile);

        boolean exists = fileSystemAdapter.exists(existingFile);

        assertTrue(exists);
    }

    @Test
    void should_ReturnFalse_WhenPathNotExists() {
        Path nonExistingFile = tempDir.resolve("non-existing.txt");

        boolean exists = fileSystemAdapter.exists(nonExistingFile);

        assertFalse(exists);
    }

    @Test
    void should_ReturnTrue_ForExistingDirectory() throws IOException {
        Path existingDirectory = tempDir.resolve("existing-dir");
        Files.createDirectory(existingDirectory);

        boolean exists = fileSystemAdapter.exists(existingDirectory);

        assertTrue(exists);
    }

    @Test
    void should_HandleLongFilePaths() throws IOException {
        StringBuilder longFileName = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longFileName.append("long");
        }
        Path longPath = tempDir.resolve(longFileName.toString() + ".txt");
        String content = "Content for long path";

        fileSystemAdapter.writeStringToFile(longPath, content);

        assertTrue(Files.exists(longPath));
        String actualContent = Files.readString(longPath);
        assertTrue(actualContent.equals(content));
    }

    @Test
    void should_HandleSpecialCharactersInContent() throws IOException {
        Path filePath = tempDir.resolve("special.txt");
        String specialContent = "Special chars: æøå äöü ñ 中文 🌟 \n\t\r\\\"'";

        fileSystemAdapter.writeStringToFile(filePath, specialContent);

        assertTrue(Files.exists(filePath));
        String actualContent = Files.readString(filePath);
        assertTrue(actualContent.equals(specialContent));
    }
}
