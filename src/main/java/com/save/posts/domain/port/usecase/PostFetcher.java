package com.save.posts.domain.port.usecase;

import java.util.List;

import com.save.posts.domain.model.Post;

import reactor.core.publisher.Mono;


public interface PostFetcher {
    Mono<List<Post>> fetchPosts();
}
