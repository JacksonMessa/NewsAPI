package com.example.NewsAPI.domain.news;

import jakarta.validation.constraints.NotBlank;

public record NewsRequestDTO(
        @NotBlank(message = "The news title is a mandatory parameter")
        String title,
        @NotBlank(message = "The news body is a mandatory parameter")
        String body
){}
