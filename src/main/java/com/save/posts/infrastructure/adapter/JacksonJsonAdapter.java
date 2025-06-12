package com.save.posts.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.save.posts.domain.port.output.JsonSerializer;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Component
public class JacksonJsonAdapter implements JsonSerializer {

    private final ObjectMapper objectMapper;

    @Override
    public String toJson(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
