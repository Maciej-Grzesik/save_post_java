package com.save.posts.domain.port.output;


public interface JsonSerializer {
    String toJson(Object object);
}
