package com.example.NewsAPI.domain.user;

public record RegisterRequestDTO(String username, String password, UserRole role) {

}
