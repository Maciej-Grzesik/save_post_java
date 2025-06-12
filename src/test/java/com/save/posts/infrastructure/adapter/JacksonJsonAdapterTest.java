package com.save.posts.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.save.posts.domain.model.Post;

@ExtendWith(MockitoExtension.class)
class JacksonJsonAdapterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectWriter objectWriter;

    @InjectMocks
    private JacksonJsonAdapter jsonAdapter;

    @Test
    void should_SerializeObjectToJson_Successfully() throws JsonProcessingException {
        Post post = new Post(1L, 1L, "Test Title", "Test Body");
        String expectedJson = "{\n  \"userId\" : 1,\n  \"id\" : 1,\n  \"title\" : \"Test Title\",\n  \"body\" : \"Test Body\"\n}";

        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(post)).thenReturn(expectedJson);

        String result = jsonAdapter.toJson(post);

        assertEquals(expectedJson, result);
    }

    @Test
    void should_SerializeNullObject() throws JsonProcessingException {
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(null)).thenReturn("null");

        String result = jsonAdapter.toJson(null);

        assertEquals("null", result);
    }

    @Test
    void should_ThrowRuntimeException_WhenSerializationFails() throws JsonProcessingException {
        Post post = new Post(1L, 1L, "Test Title", "Test Body");
        JsonProcessingException jsonException = new JsonProcessingException("Serialization failed") {
        };

        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(any())).thenThrow(jsonException);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> jsonAdapter.toJson(post));

        assertEquals("Failed to serialize object to JSON", exception.getMessage());
        assertEquals(jsonException, exception.getCause());
    }

    @Test
    void should_HandleComplexObjects() throws JsonProcessingException {
        Object object = new Object() {
            public String getName() {
                return "object";
            }

            public int getValue() {
                return 56;
            }
        };
        String expectedJson = "{\n  \"name\" : \"object\",\n  \"value\" : 56\n}";

        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(object)).thenReturn(expectedJson);

        String result = jsonAdapter.toJson(object);

        assertEquals(expectedJson, result);
    }
}
