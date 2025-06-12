package com.save.posts.domain.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.save.posts.domain.model.Post;
import com.save.posts.domain.port.output.PostSaver;
import com.save.posts.domain.port.usecase.PostFetcher;
import com.save.posts.infrastructure.exception.PostProcessingException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostFetcher postFetcher;

    @Mock
    private PostSaver postSaver;

    @InjectMocks
    private PostService postService;

    @Test
    void should_FetchAndSavePosts_Successfully() {
        Post post1 = new Post(1L, 1L, "Test Title 1", "Test Body 1");
        Post post2 = new Post(1L, 2L, "Test Title 2", "Test Body 2");
        List<Post> posts = List.of(post1, post2);

        when(postFetcher.fetchPosts()).thenReturn(Mono.just(posts));

        StepVerifier.create(postService.fetchAndSavePosts())
                .verifyComplete();

        verify(postFetcher).fetchPosts();
        verify(postSaver).savePosts(posts);
    }

    @Test
    void should_FetchAndSaveEmptyList_Successfully() {
        List<Post> emptyPosts = List.of();
        when(postFetcher.fetchPosts()).thenReturn(Mono.just(emptyPosts));

        StepVerifier.create(postService.fetchAndSavePosts())
                .verifyComplete();

        verify(postFetcher).fetchPosts();
        verify(postSaver).savePosts(emptyPosts);
    }

    @Test
    void should_ThrowPostProcessingException_WhenFetcherFails() {
        RuntimeException fetchError = new RuntimeException("API connection failed");
        when(postFetcher.fetchPosts()).thenReturn(Mono.error(fetchError));

        StepVerifier.create(postService.fetchAndSavePosts())
                .expectErrorMatches(throwable -> throwable instanceof PostProcessingException &&
                        throwable.getMessage().equals("Failed to complete post processing workflow") &&
                        throwable.getCause() == fetchError)
                .verify();
    }

    @Test
    void should_ThrowPostProcessingException_WhenSaverFails() {
        Post post = new Post(1L, 1L, "Test Title", "Test Body");
        List<Post> posts = List.of(post);

        when(postFetcher.fetchPosts()).thenReturn(Mono.just(posts));

        RuntimeException saveError = new RuntimeException("File save failed");
        doThrow(saveError).when(postSaver).savePosts(posts);

        StepVerifier.create(postService.fetchAndSavePosts())
                .expectErrorMatches(throwable -> throwable instanceof PostProcessingException &&
                        throwable.getMessage().equals("Failed to complete post processing workflow") &&
                        throwable.getCause() == saveError)
                .verify();
    }

    @Test
    void should_HandleLargeDataSet() {
        List<Post> largeBatch = generatePosts(1000);
        when(postFetcher.fetchPosts()).thenReturn(Mono.just(largeBatch));

        StepVerifier.create(postService.fetchAndSavePosts())
                .verifyComplete();

        verify(postFetcher).fetchPosts();
        verify(postSaver).savePosts(largeBatch);
    }

    @Test
    void should_PropagatePostProcessingException_FromFetcher() {
        PostProcessingException specificError = new PostProcessingException("Specific fetch error");
        when(postFetcher.fetchPosts()).thenReturn(Mono.error(specificError));

        StepVerifier.create(postService.fetchAndSavePosts())
                .expectErrorMatches(throwable -> throwable instanceof PostProcessingException &&
                        throwable.getMessage().equals("Failed to complete post processing workflow") &&
                        throwable.getCause() == specificError)
                .verify();
    }

    @Test
    void should_PropagatePostProcessingException_FromSaver() {
        Post post = new Post(1L, 1L, "Test Title", "Test Body");
        List<Post> posts = List.of(post);

        when(postFetcher.fetchPosts()).thenReturn(Mono.just(posts));

        PostProcessingException specificError = new PostProcessingException("Specific save error");
        doThrow(specificError).when(postSaver).savePosts(any());

        StepVerifier.create(postService.fetchAndSavePosts())
                .expectErrorMatches(throwable -> throwable instanceof PostProcessingException &&
                        throwable.getMessage().equals("Failed to complete post processing workflow") &&
                        throwable.getCause() == specificError)
                .verify();
    }

    private List<Post> generatePosts(int count) {
        return java.util.stream.IntStream.range(1, count + 1)
                .mapToObj(i -> new Post(1L, (long) i, "Title " + i, "Body " + i))
                .toList();
    }
}
