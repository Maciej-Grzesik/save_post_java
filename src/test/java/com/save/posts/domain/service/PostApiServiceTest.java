package com.save.posts.domain.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.save.posts.domain.model.Post;
import com.save.posts.domain.port.output.HttpClient;
import com.save.posts.infrastructure.exception.PostProcessingException;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PostApiServiceTest {

    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private PostApiService postApiService;

    private static final String API_URL = "https://jsonplaceholder.typicode.com/posts";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(postApiService, "apiUrl", API_URL);
    }

    @Test
    void should_FetchPosts_Successfully() {
        Post post1 = new Post(1L, 1L, "Test Title 1", "Test Body 1");
        Post post2 = new Post(1L, 2L, "Test Title 2", "Test Body 2");

        when(httpClient.get(eq(API_URL), eq(Post.class)))
                .thenReturn(Flux.just(post1, post2));

        StepVerifier.create(postApiService.fetchPosts())
                .expectNext(List.of(post1, post2))
                .verifyComplete();
    }

    @Test
    void should_FetchEmptyList_WhenNoPostsReturned() {
        when(httpClient.get(eq(API_URL), eq(Post.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(postApiService.fetchPosts())
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    void should_ThrowPostProcessingException_WhenHttpClientFails() {
        RuntimeException httpError = new RuntimeException("HTTP connection failed");
        when(httpClient.get(anyString(), eq(Post.class)))
                .thenReturn(Flux.error(httpError));

        StepVerifier.create(postApiService.fetchPosts())
                .expectErrorMatches(throwable -> throwable instanceof PostProcessingException &&
                        throwable.getMessage().equals("Failed to fetch posts from API") &&
                        throwable.getCause() == httpError)
                .verify();
    }

    @Test
    void should_HandleLargeNumberOfPosts() {
        List<Post> largeBatch = generatePosts(1000);
        when(httpClient.get(eq(API_URL), eq(Post.class)))
                .thenReturn(Flux.fromIterable(largeBatch));

        StepVerifier.create(postApiService.fetchPosts())
                .expectNext(largeBatch)
                .verifyComplete();
    }

    private List<Post> generatePosts(int count) {
        return java.util.stream.IntStream.range(1, count + 1)
                .mapToObj(i -> new Post(1L, (long) i, "Title " + i, "Body " + i))
                .toList();
    }
}
