package com.example.NewsAPI.controllers;

import com.example.NewsAPI.domain.news.*;
import com.example.NewsAPI.domain.services.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("news-api/news")
public class NewsController {

    @Autowired
    NewsService newsService;

    @PostMapping
    public ResponseEntity<NewsResponseDTO> create(@RequestBody @Valid NewsRequestDTO data){
        News news = newsService.create(data);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(news.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(new NewsResponseDTO("News created successfully",news.getId(),news.getTitle(), news.getBody(),news.getPublishedAt(),news.getWriter().getUsername()));
    }

    @GetMapping
    public ResponseEntity<NewsGetResponseListDTO> get(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "-1") int pageSize,
                                                        @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String writer,
                                                        @RequestParam(required = false) String publicationDate
    ){
        if (pageSize <= 0){
            List<NewsGetResponseDTO> newsList = newsService.get(title,writer,publicationDate);
            return ResponseEntity.ok().body(new NewsGetResponseListDTO("News returned successfully",newsList.size(),1,newsList));
        }else {
            NewsGetResponseListDTO response = newsService.getNewsPaged(title,writer,publicationDate,page,pageSize);
            return ResponseEntity.ok().body(response);
        }
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<NewsResponseDTO> getOne(@PathVariable UUID newsId){
        News news = newsService.getOne(newsId);
        return ResponseEntity.ok().body(new NewsResponseDTO("News found successfully",news.getId(),news.getTitle(), news.getBody(),news.getPublishedAt(),news.getWriter().getUsername()));
    }

    @PutMapping("/{newsId}")
    public ResponseEntity<NewsResponseDTO> update(@PathVariable UUID newsId, @RequestBody NewsRequestDTO data){
        News updatedNews = newsService.update(newsId,data);
        return ResponseEntity.ok().body(new NewsResponseDTO("News updated successfully",updatedNews.getId(),updatedNews.getTitle(), updatedNews.getBody(),updatedNews.getPublishedAt(),updatedNews.getWriter().getUsername()));
    }

    @DeleteMapping("/{newsId}")
    public ResponseEntity<NewsResponseDTO> delete(@PathVariable UUID newsId){
        News newsDeleted = newsService.delete(newsId);
        return ResponseEntity.ok().body(new NewsResponseDTO("News deleted successfully",newsDeleted.getId(),newsDeleted.getTitle(), newsDeleted.getBody(),newsDeleted.getPublishedAt(),newsDeleted.getWriter().getUsername()));
    }

}
