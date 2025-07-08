package com.example.NewsAPI.domain.user;

public record LoginResponseDTO(String message, String token, UserResponseDTO user) {
}
