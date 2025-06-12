package com.save.posts.domain.model;

public record Post (
    Long userId,
    Long id,
    String title,
    String body
) {

}

