package com.save.posts.infrastructure.adapter;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.save.posts.domain.port.output.HttpClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;


@RequiredArgsConstructor
@Component
public class SpringWebClientAdapter implements HttpClient {

    private final WebClient webClient;

    @Override
    public <T> Flux<T> get(String uri, Class<T> responseType) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(responseType);
    }
}
