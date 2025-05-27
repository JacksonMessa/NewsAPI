package com.example.NewsAPI.domain.news;

import java.util.Date;
import java.util.UUID;

public record NewsGetResponseDTO(UUID id, String title, String body, Date publishedAt, String writer) {
}
