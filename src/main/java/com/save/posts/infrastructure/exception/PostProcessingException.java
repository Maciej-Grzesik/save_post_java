package com.save.posts.infrastructure.exception;

public class PostProcessingException extends RuntimeException {
    
    public PostProcessingException(String message) {
        super(message);
    }
    
    public PostProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
