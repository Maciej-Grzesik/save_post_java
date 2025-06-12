package com.save.posts.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.save.posts.PostsApplication;
import com.save.posts.domain.service.PostService;

@SpringBootTest(classes = PostsApplication.class, webEnvironment = WebEnvironment.NONE)
@TestPropertySource(properties = {
        "api.url=https://jsonplaceholder.typicode.com/posts",
        "save.directory=e2e-test-output",
        "spring.main.web-application-type=none"
})
class PostsApplicationE2ETest {

    @Autowired
    private PostService postService;

    private final String testDirectory = "e2e-test-output";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        cleanupTestDirectory();
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanupTestDirectory();
    }

    @Test
    void should_RunCompleteApplication_FetchAndSavePosts() throws IOException {
        postService.fetchAndSavePosts().block();

        Path outputDirectory = Paths.get(testDirectory);

        assertTrue(Files.exists(outputDirectory), "Output directory should exist");
        assertTrue(Files.isDirectory(outputDirectory), "Output path should be directory");

        List<Path> jsonFiles = Files.list(outputDirectory)
                .filter(path -> path.toString().endsWith(".json"))
                .sorted((a, b) -> {
                    String nameA = a.getFileName().toString().replace(".json", "");
                    String nameB = b.getFileName().toString().replace(".json", "");
                    return Integer.compare(Integer.parseInt(nameA), Integer.parseInt(nameB));
                })
                .toList();

        assertEquals(100, jsonFiles.size(), "Should create exactly 100 JSON files");

        for (int i = 0; i < jsonFiles.size(); i++) {
            String expectedFileName = (i + 1) + ".json";
            String actualFileName = jsonFiles.get(i).getFileName().toString();
            assertEquals(expectedFileName, actualFileName,
                    "File should be named " + expectedFileName);
        }

        for (int i = 0; i < Math.min(5, jsonFiles.size()); i++) { // Test first 5 files
            Path file = jsonFiles.get(i);
            String content = Files.readString(file);

            JsonNode jsonNode = objectMapper.readTree(content);

            assertTrue(jsonNode.has("userId"), "JSON should have userId field");
            assertTrue(jsonNode.has("id"), "JSON should have id field");
            assertTrue(jsonNode.has("title"), "JSON should have title field");
            assertTrue(jsonNode.has("body"), "JSON should have body field");

            assertTrue(jsonNode.get("userId").isNumber(), "userId should be numeric");
            assertTrue(jsonNode.get("id").isNumber(), "id should be numeric");
            assertTrue(jsonNode.get("title").isTextual(), "title should be text");
            assertTrue(jsonNode.get("body").isTextual(), "body should be text");

            int expectedId = i + 1;
            int actualId = jsonNode.get("id").asInt();
            assertEquals(expectedId, actualId, "Post ID should match filename");

            assertTrue(jsonNode.get("title").asText().length() > 0, "Title should not be empty");
            assertTrue(jsonNode.get("body").asText().length() > 0, "Body should not be empty");
        }
        verifySpecificPost(jsonFiles.get(0), 1, 1, "sunt aut facere");
        verifySpecificPost(jsonFiles.get(1), 1, 2, "qui est esse");
    }

    @Test
    void should_HandleFileSystemPermissions() {
        postService.fetchAndSavePosts().block();

        Path outputDirectory = Paths.get(testDirectory);
        assertTrue(Files.exists(outputDirectory));
        assertTrue(Files.isReadable(outputDirectory));
        assertTrue(Files.isWritable(outputDirectory));
    }

    private void verifySpecificPost(Path filePath, int expectedUserId, int expectedId, String titleContains)
            throws IOException {
        String content = Files.readString(filePath);
        JsonNode jsonNode = objectMapper.readTree(content);

        assertEquals(expectedUserId, jsonNode.get("userId").asInt(), "UserId should match");
        assertEquals(expectedId, jsonNode.get("id").asInt(), "ID should match");
        assertTrue(jsonNode.get("title").asText().contains(titleContains),
                "Title should contain: " + titleContains);
    }

    private void cleanupTestDirectory() throws IOException {
        Path testDir = Paths.get(testDirectory);
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {

                        }
                    });
        }
    }
}
