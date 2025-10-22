package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.domain.news.*;
import com.example.NewsAPI.domain.repositories.NewsRepository;
import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.user.User;
import com.example.NewsAPI.exception.BelongsToAnotherWriterException;
import com.example.NewsAPI.exception.DateConvertException;
import com.example.NewsAPI.exception.NewsNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class NewsService {
    @Autowired
    NewsRepository newsRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateService dateService;

    @Autowired
    Clock clock;

    public News create(NewsRequestDTO data){
        News news = new News();

        news.setTitle(data.title());
        news.setBody(data.body());

        news.setPublishedAt(Date.from(clock.instant()));

        String writerUsername = tokenService.recoverTokenAndGetUsername();
        UserDetails writer = userRepository.findByUsername(writerUsername);

        news.setWriter((User) writer);

        return newsRepository.save(news);
    }

    public List<NewsGetResponseDTO> get(String title,String writer,String publicationDate){
        Date startDate;
        Date endDate;

        startDate = dateService.definesStartDate(publicationDate);
        endDate = dateService.definesEndDate(publicationDate,startDate);


        List<News> newsList = newsRepository.findNews(title,writer,startDate,endDate);

        return newsList.stream().map(news -> new NewsGetResponseDTO(
                                                        news.getId(),
                                                        news.getTitle(),
                                                        news.getBody(),
                                                        news.getPublishedAt(),
                                                        news.getWriter().getUsername())
                                                    ).toList();

    }

    public NewsGetResponseListDTO getNewsPaged(String title, String writer, String publicationDate, int page, int pageSize){
        Date startDate;
        Date endDate;

        startDate = dateService.definesStartDate(publicationDate);
        endDate = dateService.definesEndDate(publicationDate,startDate);

        Pageable pageable = PageRequest.of(page,pageSize);
        Page<News> newsPage = newsRepository.findNews(title,writer,startDate,endDate,pageable);
        List<NewsGetResponseDTO> newsListResponse = newsPage.map(news -> new NewsGetResponseDTO(
                                                        news.getId(),
                                                        news.getTitle(),
                                                        news.getBody(),
                                                        news.getPublishedAt(),
                                                        news.getWriter().getUsername())
                                                    ).stream().toList();

        return new NewsGetResponseListDTO("News returned successfully",newsPage.getTotalElements(),newsPage.getTotalPages(),newsListResponse);
    }



    public News getOne(UUID newsID){
        return newsRepository.findById(newsID)
                .orElseThrow(() -> new NewsNotFoundException("News Not Found"));
    }

    public News update(UUID newsID, NewsRequestDTO data){

        if (!newsRepository.existsById(newsID)){
            throw new NewsNotFoundException("News Not Found");
        }

        News oldNews = getOne(newsID);

        String loggedUsername = tokenService.recoverTokenAndGetUsername();

        if (!loggedUsername.equals(oldNews.getWriter().getUsername())){
            throw new BelongsToAnotherWriterException("You are not authorized to update this news because it belongs to another user.");
        }

        News updatedNews = new News(
            oldNews.getId(),
            data.title() != null ? data.title() : oldNews.getTitle(),
            data.body() != null ? data.body() : oldNews.getBody(),
            oldNews.getPublishedAt(),
            oldNews.getWriter()
        );

        return newsRepository.save(updatedNews);
    }

    public News delete(UUID newsID){

        if (!newsRepository.existsById(newsID)){
            throw new NewsNotFoundException("News Not Found");
        }

        News news = getOne(newsID);

        String loggedUsername = tokenService.recoverTokenAndGetUsername();

        if (!loggedUsername.equals(news.getWriter().getUsername())){
            throw new BelongsToAnotherWriterException("You are not authorized to update this news because it belongs to another user.");
        }

        newsRepository.delete(news);

        return news;
    }




}
