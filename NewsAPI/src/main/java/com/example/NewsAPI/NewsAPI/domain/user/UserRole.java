package com.example.NewsAPI.NewsAPI.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {
    WRITER("writer"),
    READER("reader");

    String role;
}
