package com.save.posts.domain.service;

import org.springframework.stereotype.Service;

import com.save.posts.domain.port.output.PostSaver;
import com.save.posts.domain.port.usecase.PostFetcher;
import com.save.posts.infrastructure.exception.PostProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostFetcher postFetcher;
    private final PostSaver postSaver;

    public Mono<Void> fetchAndSavePosts() {
        log.info("Starting post processing workflow");

        return postFetcher.fetchPosts()
                .doOnNext(posts -> postSaver.savePosts(posts))
                .then()
                .doOnSuccess(v -> log.info("Post processing workflow completed successfully"))
                .doOnError(error -> log.error("Error in post processing workflow", error))
                .onErrorMap(throwable -> new PostProcessingException("Failed to complete post processing workflow",
                        throwable));
    }
}
