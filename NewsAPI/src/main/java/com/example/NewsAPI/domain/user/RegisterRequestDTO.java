package com.example.NewsAPI.domain.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDTO(
        @NotBlank(message = "The username is a mandatory parameter")
        String username,
        @NotBlank(message = "The password is a mandatory parameter")
        String password,
        @NotNull(message = "The user role is a mandatory parameter and should be WRITER or READER")
        UserRole role
) { }
