package com.example.NewsAPI.domain.repositories;

import com.example.NewsAPI.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    UserDetails findByUsername(String username);
    User findUserByUsername(String username);
}
