package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.domain.infra.security.TokenService;
import com.example.NewsAPI.domain.news.News;
import com.example.NewsAPI.domain.news.NewsGetResponseDTO;
import com.example.NewsAPI.domain.news.NewsRequestDTO;
import com.example.NewsAPI.domain.repositories.NewsRepository;
import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

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

    public List<NewsGetResponseDTO> get(){
        List<News> newsList = newsRepository.findAll();
        return newsList.stream().map(news -> new NewsGetResponseDTO(
                news.getId(),
                news.getTitle(),
                news.getBody(),
                news.getPublished_at(),
                news.getWriter().getUsername()))
                .toList();
    }

    public List<NewsGetResponseDTO> getNewsPaged(int page, int pageSize){
        Pageable pageable = PageRequest.of(page,pageSize);
        Page<News> newsPage = newsRepository.findAll(pageable);
        return newsPage.map(news -> new NewsGetResponseDTO(
                        news.getId(),
                        news.getTitle(),
                        news.getBody(),
                        news.getPublished_at(),
                        news.getWriter().getUsername()))
                .stream().toList();
    }


}
