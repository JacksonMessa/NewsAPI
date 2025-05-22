package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.domain.infra.security.TokenService;
import com.example.NewsAPI.domain.news.News;
import com.example.NewsAPI.domain.news.NewsRequestDTO;
import com.example.NewsAPI.domain.repositories.NewsRepository;
import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class NewsService {
    @Autowired
    NewsRepository newsRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepository userRepository;

    public News create(NewsRequestDTO data){
        News news = new News();

        news.setTitle(data.title());
        news.setBody(data.body());
        news.setPublished_at(Date.from(LocalDateTime.now().toInstant(ZoneOffset.of("-03:00"))));

        String writerUsername = tokenService.recoverTokenAndGetUsername();
        UserDetails writer = userRepository.findByUsername(writerUsername);

        news.setWriter((User) writer);

        return newsRepository.save(news);
    }
}
