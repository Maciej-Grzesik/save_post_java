package com.save.posts.domain.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.save.posts.domain.model.Post;
import com.save.posts.domain.port.output.FileSystemService;
import com.save.posts.domain.port.output.JsonSerializer;
import com.save.posts.domain.port.output.PostSaver;
import com.save.posts.infrastructure.exception.PostProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Service
public class PostFileService implements PostSaver {

    private final JsonSerializer jsonSerializer;
    private final FileSystemService fileSystemService;

    @Value("${save.directory}")
    private String saveDirectory;

    @Override
    public void savePosts(List<Post> posts) {
        try {
            createDirectoryIfNotExists();
            for (Post post : posts) {
                savePostToFile(post);
            }
            log.info("Successfully saved {} posts to directory: {}", posts.size(), saveDirectory);
        } catch (IOException e) {
            log.error("IO error saving posts", e);
            throw new PostProcessingException("Failed to save posts due to IO error", e);
        } catch (Exception e) {
            log.error("Unexpected error saving posts", e);
            throw new PostProcessingException("Failed to save posts", e);
        }
    }

    private void createDirectoryIfNotExists() throws IOException {
        Path directoryPath = Paths.get(saveDirectory);
        if (!fileSystemService.exists(directoryPath)) {
            fileSystemService.createDirectoriesIfNotExists(directoryPath);
            log.info("Created directory: {}", saveDirectory);
        }
    }

    private void savePostToFile(Post post) throws IOException {
        String fileName = post.id() + ".json";
        Path filePath = Paths.get(saveDirectory, fileName);

        String jsonContent = jsonSerializer.toJson(post);
        fileSystemService.writeStringToFile(filePath, jsonContent);

        log.debug("Saved post {} to file: {}", post.id(), filePath);
    }
}
