package com.save.posts.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.save.posts.domain.model.Post;
import com.save.posts.domain.port.output.HttpClient;
import com.save.posts.domain.port.usecase.PostFetcher;
import com.save.posts.infrastructure.exception.PostProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostApiService implements PostFetcher {

    private final HttpClient httpClient;

    @Value("${api.url}")
    private String apiUrl;

    @Override
    public Mono<List<Post>> fetchPosts() {
        log.info("Fetching posts from: {}", apiUrl);

        return httpClient.get(apiUrl, Post.class)
                .collectList()
                .doOnNext(posts -> log.info("Fetched {} posts from API", posts.size()))
                .doOnError(error -> log.error("Error fetching posts from API", error))
                .onErrorMap(throwable -> new PostProcessingException("Failed to fetch posts from API", throwable));
    }
}
