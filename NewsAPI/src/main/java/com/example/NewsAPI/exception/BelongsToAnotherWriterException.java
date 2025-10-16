package com.example.NewsAPI.exception;

public class BelongsToAnotherWriterException extends RuntimeException {
    public BelongsToAnotherWriterException(String message) {
        super(message);
    }
}
