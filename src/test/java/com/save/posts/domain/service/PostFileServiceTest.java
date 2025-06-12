package com.save.posts.domain.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.save.posts.domain.model.Post;
import com.save.posts.domain.port.output.FileSystemService;
import com.save.posts.domain.port.output.JsonSerializer;
import com.save.posts.infrastructure.exception.PostProcessingException;

@ExtendWith(MockitoExtension.class)
class PostFileServiceTest {

    @Mock
    private JsonSerializer jsonSerializer;

    @Mock
    private FileSystemService fileSystemService;

    @InjectMocks
    private PostFileService postFileService;

    private static final String SAVE_DIRECTORY = "test-output";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(postFileService, "saveDirectory", SAVE_DIRECTORY);
    }

    @Test
    void should_SavePosts_Successfully() throws IOException {
        Post post1 = new Post(1L, 1L, "Test Title 1", "Test Body 1");
        Post post2 = new Post(1L, 2L, "Test Title 2", "Test Body 2");
        List<Post> posts = List.of(post1, post2);

        Path directoryPath = Paths.get(SAVE_DIRECTORY);
        when(fileSystemService.exists(directoryPath)).thenReturn(true);
        when(jsonSerializer.toJson(post1)).thenReturn("{\"id\":1,\"title\":\"Test Title 1\"}");
        when(jsonSerializer.toJson(post2)).thenReturn("{\"id\":2,\"title\":\"Test Title 2\"}");

        postFileService.savePosts(posts);

        verify(fileSystemService).exists(directoryPath);
        verify(fileSystemService, never()).createDirectoriesIfNotExists(any());
        verify(jsonSerializer, times(2)).toJson(any(Post.class));
        verify(fileSystemService, times(2)).writeStringToFile(any(Path.class), anyString());

        verify(fileSystemService).writeStringToFile(
                eq(Paths.get(SAVE_DIRECTORY, "1.json")),
                eq("{\"id\":1,\"title\":\"Test Title 1\"}"));
        verify(fileSystemService).writeStringToFile(
                eq(Paths.get(SAVE_DIRECTORY, "2.json")),
                eq("{\"id\":2,\"title\":\"Test Title 2\"}"));
    }

    @Test
    void should_CreateDirectory_WhenNotExists() throws IOException {
        Post post = new Post(1L, 1L, "Test Title", "Test Body");
        List<Post> posts = List.of(post);

        Path directoryPath = Paths.get(SAVE_DIRECTORY);
        when(fileSystemService.exists(directoryPath)).thenReturn(false);
        when(jsonSerializer.toJson(post)).thenReturn("{\"id\":1}");

        postFileService.savePosts(posts);

        verify(fileSystemService).exists(directoryPath);
        verify(fileSystemService).createDirectoriesIfNotExists(directoryPath);
        verify(fileSystemService).writeStringToFile(any(Path.class), anyString());
    }

    @Test
    void should_SaveEmptyList_Successfully() throws IOException {
        List<Post> emptyPosts = List.of();
        Path directoryPath = Paths.get(SAVE_DIRECTORY);
        when(fileSystemService.exists(directoryPath)).thenReturn(true);

        postFileService.savePosts(emptyPosts);

        verify(fileSystemService).exists(directoryPath);
        verify(jsonSerializer, never()).toJson(any());
        verify(fileSystemService, never()).writeStringToFile(any(Path.class), anyString());
    }

    @Test
    void should_ThrowPostProcessingException_WhenDirectoryCreationFails() throws IOException {
        Post post = new Post(1L, 1L, "Test Title", "Test Body");
        List<Post> posts = List.of(post);

        Path directoryPath = Paths.get(SAVE_DIRECTORY);
        when(fileSystemService.exists(directoryPath)).thenReturn(false);

        IOException ioException = new IOException("Permission denied");
        doThrow(ioException).when(fileSystemService).createDirectoriesIfNotExists(directoryPath);

        PostProcessingException exception = assertThrows(
                PostProcessingException.class,
                () -> postFileService.savePosts(posts));

        assertEquals("Failed to save posts due to IO error", exception.getMessage());
        assertEquals(ioException, exception.getCause());
        verify(jsonSerializer, never()).toJson(any());
    }

    @Test
    void should_ThrowPostProcessingException_WhenFileWriteFails() throws IOException {
        Post post = new Post(1L, 1L, "Test Title", "Test Body");
        List<Post> posts = List.of(post);

        Path directoryPath = Paths.get(SAVE_DIRECTORY);
        when(fileSystemService.exists(directoryPath)).thenReturn(true);
        when(jsonSerializer.toJson(post)).thenReturn("{\"id\":1}");

        IOException ioException = new IOException("Disk full");
        doThrow(ioException).when(fileSystemService).writeStringToFile(any(Path.class), anyString());

        PostProcessingException exception = assertThrows(
                PostProcessingException.class,
                () -> postFileService.savePosts(posts));

        assertEquals("Failed to save posts due to IO error", exception.getMessage());
        assertEquals(ioException, exception.getCause());
    }

    @Test
    void should_ThrowPostProcessingException_WhenJsonSerializationFails() throws IOException {
        Post post = new Post(1L, 1L, "Test Title", "Test Body");
        List<Post> posts = List.of(post);

        Path directoryPath = Paths.get(SAVE_DIRECTORY);
        when(fileSystemService.exists(directoryPath)).thenReturn(true);

        RuntimeException jsonException = new RuntimeException("JSON serialization failed");
        when(jsonSerializer.toJson(post)).thenThrow(jsonException);

        PostProcessingException exception = assertThrows(
                PostProcessingException.class,
                () -> postFileService.savePosts(posts));

        assertEquals("Failed to save posts", exception.getMessage());
        assertEquals(jsonException, exception.getCause());
        verify(fileSystemService, never()).writeStringToFile(any(Path.class), anyString());
    }

    @Test
    void should_HandleLargeNumberOfPosts() throws IOException {
        List<Post> largeBatch = generatePosts(100);
        Path directoryPath = Paths.get(SAVE_DIRECTORY);
        when(fileSystemService.exists(directoryPath)).thenReturn(true);

        for (Post post : largeBatch) {
            when(jsonSerializer.toJson(post)).thenReturn("{\"id\":" + post.id() + "}");
        }

        postFileService.savePosts(largeBatch);

        verify(jsonSerializer, times(100)).toJson(any(Post.class));
        verify(fileSystemService, times(100)).writeStringToFile(any(Path.class), anyString());
    }

    private List<Post> generatePosts(int count) {
        return java.util.stream.IntStream.range(1, count + 1)
                .mapToObj(i -> new Post(1L, (long) i, "Title " + i, "Body " + i))
                .toList();
    }
}
