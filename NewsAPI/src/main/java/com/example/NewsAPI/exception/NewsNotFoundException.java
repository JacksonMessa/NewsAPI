package com.example.NewsAPI.exception;

public class NewsNotFoundException extends RuntimeException {
    public NewsNotFoundException(String message) {
        super(message);
    }
}
