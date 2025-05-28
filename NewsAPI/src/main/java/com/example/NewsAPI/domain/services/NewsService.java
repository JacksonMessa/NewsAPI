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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
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
        news.setPublishedAt(Date.from(LocalDateTime.now().toInstant(ZoneOffset.of("-03:00"))));

        String writerUsername = tokenService.recoverTokenAndGetUsername();
        UserDetails writer = userRepository.findByUsername(writerUsername);

        news.setWriter((User) writer);

        return newsRepository.save(news);
    }

    public List<NewsGetResponseDTO> get(String title,String writer,String publicationDate){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Date startDate;
        Date endDate;

        try {
            if (publicationDate ==null){

                startDate = simpleDateFormat.parse("01/01/0001");
                endDate = simpleDateFormat.parse("31/12/9999");


            }else {
                startDate = simpleDateFormat.parse(publicationDate);
                endDate = addOneDayToDate(startDate);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Error while corventing date");
        }

        List<News> newsList = newsRepository.findNews(title,writer,startDate,endDate);
        return newsList.stream().map(news -> new NewsGetResponseDTO(
                news.getId(),
                news.getTitle(),
                news.getBody(),
                news.getPublishedAt(),
                news.getWriter().getUsername()))
                .toList();
    }

    public List<NewsGetResponseDTO> getNewsPaged(String title,String writer,String publicationDate,int page, int pageSize){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Date startDate;
        Date endDate;

        try {
            if (publicationDate ==null){

                startDate = simpleDateFormat.parse("01/01/0001");
                endDate = simpleDateFormat.parse("31/12/9999");


            }else {
                startDate = simpleDateFormat.parse(publicationDate);
                endDate = addOneDayToDate(startDate);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Error while corventing date");
        }

        Pageable pageable = PageRequest.of(page,pageSize);
        Page<News> newsPage = newsRepository.findNews(title,writer,startDate,endDate,pageable);
        return newsPage.map(news -> new NewsGetResponseDTO(
                        news.getId(),
                        news.getTitle(),
                        news.getBody(),
                        news.getPublishedAt(),
                        news.getWriter().getUsername()))
                .stream().toList();
    }

    private Date addOneDayToDate(Date date){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE,1);

        return calendar.getTime();
    }

}
