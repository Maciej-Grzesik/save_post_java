package com.save.posts.domain.port.output;

import reactor.core.publisher.Flux;


public interface HttpClient {
    <T> Flux<T> get(String uri, Class<T> responseType);
}
