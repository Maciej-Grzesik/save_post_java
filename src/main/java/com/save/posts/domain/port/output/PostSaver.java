package com.save.posts.domain.port.output;

import java.util.List;

import com.save.posts.domain.model.Post;


public interface PostSaver {
    void savePosts(List<Post> posts);
}
