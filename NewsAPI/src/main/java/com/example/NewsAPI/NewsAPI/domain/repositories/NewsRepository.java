package com.example.NewsAPI.NewsAPI.domain.repositories;

import com.example.NewsAPI.NewsAPI.domain.news.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
}
