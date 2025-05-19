package com.example.NewsAPI.NewsAPI.Domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    JOURNALIST("journalist"),
    READER("reader");

    private String role;
}
