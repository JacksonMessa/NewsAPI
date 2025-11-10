package com.example.NewsAPI.domain.user;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "The username is a mandatory parameter")
        String username,
        @NotBlank(message = "The password is a mandatory parameter")
        String password
) { }
