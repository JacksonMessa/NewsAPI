package com.example.NewsAPI.factory;

import com.example.NewsAPI.domain.news.News;
import com.example.NewsAPI.domain.news.NewsGetResponseDTO;
import com.example.NewsAPI.domain.news.NewsRequestDTO;
import com.example.NewsAPI.domain.user.User;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NewsTestFactory {

    public static News buildOne(){
        return new News(
            UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379"),
            "TitleTest",
            "BodyTest",
            Date.from(Instant.parse("2025-10-14T00:00:00Z")),
            UserTestFactory.buildOne()
        );
    }

    public static News buildOne(String title){
        return new News(
                UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379"),
                title,
                "BodyTest",
                Date.from(Instant.parse("2025-10-14T00:00:00Z")),
                UserTestFactory.buildOne()
        );
    }

    public static News buildOne(UUID uuid){
        return new News(
                uuid,
                "TitleTest",
                "BodyTest",
                Date.from(Instant.parse("2025-10-14T00:00:00Z")),
                UserTestFactory.buildOne()
        );
    }

    public static News buildOne(Date publicationDate){
        return new News(
                UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379"),
                "TitleTest",
                "BodyTest",
                publicationDate,
                UserTestFactory.buildOne()
        );
    }

    public static News buildOne(UUID uuid,Date publicationDate){
        return new News(
                uuid,
                "TitleTest",
                "BodyTest",
                publicationDate,
                UserTestFactory.buildOne()
        );
    }


    public static News buildOne(UUID uuid, NewsRequestDTO data, User writer){
        return new News(
                uuid,
                data.title(),
                data.body(),
                Date.from(Instant.parse("2025-10-14T00:00:00Z")),
                writer
        );
    }


    public static News buildOne(NewsRequestDTO data){
        return new News(
                UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379"),
                data.title(),
                data.body(),
                Date.from(Instant.parse("2025-10-14T00:00:00Z")),
                UserTestFactory.buildOne()
        );
    }

    public static News buildOne(UUID uuid, User writer){
        return new News(
                uuid,
                "TitleTest",
                "BodyTest",
                Date.from(Instant.parse("2025-10-14T00:00:00Z")),
                writer
        );
    }

    public static News buildOne(NewsRequestDTO data, User writer){
        return new News(
                UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379"),
                data.title(),
                data.body(),
                Date.from(Instant.parse("2025-10-14T00:00:00Z")),
                writer
        );
    }

    public static News buildOne(String title, User user, Date publicationDate){
        return new News(
                UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379"),
                title,
                "BodyTest",
                publicationDate,
                user
        );
    }

    public static News buildOne(UUID newsId, NewsRequestDTO data, User user, Date publicationDate){
        return new News(
                newsId,
                data.title(),
                data.body(),
                publicationDate,
                user
        );
    }

    public static News buildOneWithoutId(String title, User user, Date publicationDate){
        return new News(
                null,
                title,
                "BodyTest",
                publicationDate,
                user
        );
    }

    public static List<NewsGetResponseDTO> buildGetDTOList(List<News> newsList){
        return newsList.stream().map(news -> new NewsGetResponseDTO(
                news.getId(),
                news.getTitle(),
                news.getBody(),
                news.getPublishedAt(),
                news.getWriter().getUsername()
        )).toList();
    }

}
