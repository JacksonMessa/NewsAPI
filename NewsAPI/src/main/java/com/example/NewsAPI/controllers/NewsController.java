package com.example.NewsAPI.controllers;

import com.example.NewsAPI.domain.news.News;
import com.example.NewsAPI.domain.news.NewsGetResponseDTO;
import com.example.NewsAPI.domain.news.NewsRequestDTO;
import com.example.NewsAPI.domain.news.NewsResponseDTO;
import com.example.NewsAPI.domain.services.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("news-api/news")
public class NewsController {

    @Autowired
    NewsService newsService;

    @PostMapping
    public ResponseEntity<NewsResponseDTO> create(@RequestBody NewsRequestDTO data){
        News news = newsService.create(data);
        return ResponseEntity.ok().body(new NewsResponseDTO("News created successfully",news.getId(),news.getTitle(), news.getBody(),news.getPublished_at(),news.getWriter().getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<NewsGetResponseDTO>> get(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "-1") int pageSize,
                                                        @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String writer
    ){
        List<NewsGetResponseDTO> newsList;
        if (pageSize <= 0){
            newsList = newsService.get(title,writer);
        }else {
            newsList = newsService.getNewsPaged(title,writer,page,pageSize);
        }

        return ResponseEntity.ok().body(newsList);
    }
}
