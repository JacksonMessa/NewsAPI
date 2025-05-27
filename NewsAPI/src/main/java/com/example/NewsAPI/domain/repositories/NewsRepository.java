package com.example.NewsAPI.domain.repositories;

import com.example.NewsAPI.domain.news.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
    @Query("SELECT n FROM News n " +
            "JOIN n.writer w " +
            "WHERE (:title IS NULL OR n.title LIKE %:title%) AND " +
            "(:writer IS NULL OR w.username = :writer)")
    public List<News> findNews(String title,String writer);

    @Query("SELECT n FROM News n " +
            "JOIN n.writer w " +
            "WHERE (:title IS NULL OR n.title LIKE %:title%) AND " +
            "(:writer IS NULL OR w.username = :writer)")
    public Page<News> findNews(String title, String writer, Pageable pageable);
}
