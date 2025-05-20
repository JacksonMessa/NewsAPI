package com.example.NewsAPI.NewsAPI.domain.repositories;

import com.example.NewsAPI.NewsAPI.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
}
