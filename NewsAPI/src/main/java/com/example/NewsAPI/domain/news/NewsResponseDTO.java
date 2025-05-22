package com.example.NewsAPI.domain.news;

import com.example.NewsAPI.domain.user.User;

import java.util.Date;
import java.util.UUID;

public record NewsResponseDTO(String message, UUID id, String title, String body, Date publishedAt, String writer) {
}
