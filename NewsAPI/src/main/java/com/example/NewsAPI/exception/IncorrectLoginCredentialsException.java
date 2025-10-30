package com.example.NewsAPI.exception;

public class IncorrectLoginCredentialsException extends RuntimeException {
    public IncorrectLoginCredentialsException(String message) {
        super(message);
    }
}
