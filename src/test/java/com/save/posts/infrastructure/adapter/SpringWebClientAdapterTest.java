package com.save.posts.infrastructure.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.save.posts.domain.model.Post;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SpringWebClientAdapterTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private SpringWebClientAdapter webClientAdapter;

    @Test
    void should_FetchData_Successfully() {
        String uri = "https://jsonplaceholder.typicode.com/posts";
        Post post1 = new Post(1L, 1L, "Test Title 1", "Test Body 1");
        Post post2 = new Post(1L, 2L, "Test Title 2", "Test Body 2");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Post.class)).thenReturn(Flux.just(post1, post2));

        StepVerifier.create(webClientAdapter.get(uri, Post.class))
                .expectNext(post1)
                .expectNext(post2)
                .verifyComplete();
    }

    @Test
    void should_ReturnEmptyFlux_WhenNoDataReturned() {
        String uri = "https://jsonplaceholder.typicode.com/posts";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Post.class)).thenReturn(Flux.empty());

        StepVerifier.create(webClientAdapter.get(uri, Post.class))
                .verifyComplete();
    }

    @Test
    void should_PropagateError_WhenWebClientFails() {
        String uri = "https://jsonplaceholder.typicode.com/posts";
        WebClientResponseException exception = WebClientResponseException.create(
                500, "Internal Server Error", null, null, null);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Post.class)).thenReturn(Flux.error(exception));

        StepVerifier.create(webClientAdapter.get(uri, Post.class))
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void should_HandleDifferentResponseTypes() {
        String uri = "https://api.example.com/users";
        String user1 = "User1";
        String user2 = "User2";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(user1, user2));

        StepVerifier.create(webClientAdapter.get(uri, String.class))
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();
    }

    @Test
    void should_HandleNetworkTimeout() {
        String uri = "https://slow-api.example.com/posts";
        RuntimeException timeoutException = new RuntimeException("Timeout");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Post.class)).thenReturn(Flux.error(timeoutException));

        StepVerifier.create(webClientAdapter.get(uri, Post.class))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void should_HandleLargeDataStream() {
        String uri = "https://jsonplaceholder.typicode.com/posts";
        Flux<Post> largeBatch = Flux.range(1, 1000)
                .map(i -> new Post(1L, (long) i, "Title " + i, "Body " + i));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Post.class)).thenReturn(largeBatch);

        StepVerifier.create(webClientAdapter.get(uri, Post.class))
                .expectNextCount(1000)
                .verifyComplete();
    }
}
