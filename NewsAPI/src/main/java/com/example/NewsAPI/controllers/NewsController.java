package com.example.NewsAPI.controllers;

import com.example.NewsAPI.domain.news.*;
import com.example.NewsAPI.domain.services.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("news-api/news")
public class NewsController {

    @Autowired
    NewsService newsService;

    @PostMapping
    public ResponseEntity<NewsResponseDTO> create(@RequestBody NewsRequestDTO data){
        News news = newsService.create(data);
        return ResponseEntity.ok().body(new NewsResponseDTO("News created successfully",news.getId(),news.getTitle(), news.getBody(),news.getPublishedAt(),news.getWriter().getUsername()));
    }

    @GetMapping
    public ResponseEntity<NewsGetResponseListDTO> get(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "-1") int pageSize,
                                                        @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String writer,
                                                        @RequestParam(required = false) String publicationDate
    ){
        ResponseEntity<NewsGetResponseListDTO> newsList;
        if (pageSize <= 0){
            newsList = newsService.get(title,writer,publicationDate);
        }else {
            newsList = newsService.getNewsPaged(title,writer,publicationDate,page,pageSize);
        }

        return newsList;
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<NewsResponseDTO> getOne(@PathVariable UUID newsId){
        News news = newsService.getOne(newsId);
        return ResponseEntity.ok().body(new NewsResponseDTO("News found successfully",news.getId(),news.getTitle(), news.getBody(),news.getPublishedAt(),news.getWriter().getUsername()));
    }

    @PutMapping("/{newsId}")
    public ResponseEntity<NewsResponseDTO> update(@PathVariable UUID newsId, @RequestBody NewsRequestDTO data){
        return newsService.update(newsId,data);
    }

    @DeleteMapping("/{newsId}")
    public ResponseEntity<NewsResponseDTO> delete(@PathVariable UUID newsId){
        return newsService.delete(newsId);
    }

}
