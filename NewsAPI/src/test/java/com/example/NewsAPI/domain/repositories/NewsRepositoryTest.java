package com.example.NewsAPI.domain.repositories;

import com.example.NewsAPI.domain.news.News;
import com.example.NewsAPI.domain.user.User;
import com.example.NewsAPI.factory.NewsTestFactory;
import com.example.NewsAPI.factory.UserTestFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NewsRepositoryTest {
    @Autowired
    NewsRepository newsRepository;

    @Autowired
    EntityManager entityManager;

    @Nested
    class listFindNews{
        @Test
        @DisplayName("Should successfully filter news by all criteria and return in descending order")
        void listFindNewsTestSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            Date startDate = Date.from(Instant.parse("2025-11-12T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-11-13T00:00:00Z"));

            User matchedWriter = buildAndPersistsUser(writerUsername);

            News matchedNews1 = buildAndPersistsNews(title.concat("1"),matchedWriter,Date.from(Instant.parse("2025-11-12T14:00:00Z")));
            News matchedNews2 = buildAndPersistsNews(title.concat("2"),matchedWriter,Date.from(Instant.parse("2025-11-12T12:00:00Z")));

            List<News> newsListExpected = List.of(matchedNews1,matchedNews2);

            User unmatchedWriter = buildAndPersistsUser("WriterUnmatched");

            News unmatchedNews = buildAndPersistsNews("TitleUnmatched",unmatchedWriter,Date.from(Instant.parse("2025-11-15T00:00:00Z")));
            //Act
            List<News> newsListReturned = newsRepository.findNews(title,writerUsername,startDate,endDate);

            //Assert
            assertEquals(2,newsListReturned.size());
            assertThat(newsListReturned)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(newsListExpected);
            assertFalse(newsListReturned.contains(unmatchedNews));
        }

        @Test
        @DisplayName("Should return all news when filters is null and extreme dates")
        void listFindNewsTestNullParamsSuccess(){
            //Arrange
            Date startDate = Date.from(Instant.parse("0001-01-01T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("9999-12-31T00:00:00Z"));

            User writer = buildAndPersistsUser("WriterTest");

            News news1 = buildAndPersistsNews("TitleTest1",writer,Date.from(Instant.parse("2025-11-15T00:00:00Z")));
            News news2 = buildAndPersistsNews("TitleTest2",writer,Date.from(Instant.parse("2025-11-12T14:00:00Z")));

            User otherWriter = buildAndPersistsUser("UserOther");

            News news3 = buildAndPersistsNews("Other",otherWriter,Date.from(Instant.parse("2025-11-12T12:00:00Z")));

            List<News> newsListExpected = List.of(news1,news2,news3);
            //Act
            List<News> newsListReturned = newsRepository.findNews(null,null,startDate,endDate);

            //Assert
            assertEquals(3,newsListReturned.size());
            assertThat(newsListReturned)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(newsListExpected);
        }

        @Test
        @DisplayName("Should return empty list when the news doesn't match the filters")
        void listFindNewsTestEmptyListParamsSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            Date startDate = Date.from(Instant.parse("2025-11-12T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-11-13T00:00:00Z"));

            User writer1 = buildAndPersistsUser("Other");
            User writer2 = buildAndPersistsUser(writerUsername);

            buildAndPersistsNews("OtherTitle",writer1,Date.from(Instant.parse("2025-11-18T12:00:00Z")));
            buildAndPersistsNews("TitleTest",writer1,Date.from(Instant.parse("2025-11-12T20:00:00Z")));
            buildAndPersistsNews("Third",writer2,Date.from(Instant.parse("2025-11-15T00:00:00Z")));
            //Act
            List<News> newsListReturned = newsRepository.findNews(title,writerUsername,startDate,endDate);

            //Assert
            assertTrue(newsListReturned.isEmpty());
        }
    }

    @Nested
    class pageFindNews{
        @Test
        @DisplayName("Should successfully filter news by all criteria and return news from the page in descending order")
        void pageFindNewsTestSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            Date startDate = Date.from(Instant.parse("2025-11-12T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-11-13T00:00:00Z"));
            int page = 1;
            int pageSize = 1;
            Pageable pageable = PageRequest.of(page,pageSize);

            User matchedWriter = buildAndPersistsUser(writerUsername);

            News matchedNews1 = buildAndPersistsNews(title.concat("1"),matchedWriter,Date.from(Instant.parse("2025-11-12T14:00:00Z")));
            News matchedNews2 = buildAndPersistsNews(title.concat("2"),matchedWriter,Date.from(Instant.parse("2025-11-12T12:00:00Z")));

            User unmatchedWriter = buildAndPersistsUser("WriterUnmatched");

            News unmatchedNews = buildAndPersistsNews("TitleUnmatched",unmatchedWriter,Date.from(Instant.parse("2025-11-15T00:00:00Z")));
            //Act
            Page<News> newsPageReturned = newsRepository.findNews(title,writerUsername,startDate,endDate,pageable);

            //Assert
            assertEquals(1,newsPageReturned.getContent().size());
            assertEquals(2,newsPageReturned.getTotalElements());
            assertEquals(2,newsPageReturned.getTotalPages());

            assertThat(newsPageReturned)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .contains(matchedNews2);
            assertFalse(newsPageReturned.getContent().contains(matchedNews1));
            assertFalse(newsPageReturned.getContent().contains(unmatchedNews));
        }

        @Test
        @DisplayName("Should return all elements across pages when filters are ignored")
        void pageFindNewsTestNullParamsSuccess(){
            //Arrange
            Date startDate = Date.from(Instant.parse("0001-01-01T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("9999-12-31T00:00:00Z"));
            int page = 1;
            int pageSize = 1;
            Pageable pageable = PageRequest.of(page,pageSize);

            User writer = buildAndPersistsUser("WriterTest");

            News news1 = buildAndPersistsNews("TitleTest1",writer,Date.from(Instant.parse("2025-11-15T00:00:00Z")));
            News news2 = buildAndPersistsNews("TitleTest2",writer,Date.from(Instant.parse("2025-11-12T14:00:00Z")));

            User otherWriter = buildAndPersistsUser("UserOther");

            News news3 = buildAndPersistsNews("Other",otherWriter,Date.from(Instant.parse("2025-11-12T12:00:00Z")));

            //Act
            Page<News> newsPageReturned = newsRepository.findNews(null,null,startDate,endDate,pageable);

            //Assert
            assertEquals(1,newsPageReturned.getContent().size());
            assertEquals(3,newsPageReturned.getTotalElements());
            assertEquals(3,newsPageReturned.getTotalPages());

            assertThat(newsPageReturned)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .contains(news2);
            assertFalse(newsPageReturned.getContent().contains(news1));
            assertFalse(newsPageReturned.getContent().contains(news3));
        }

        @Test
        @DisplayName("Should return empty page when the news doesn't match the filters")
        void pageFindNewsTestNotMatchingFiltersEmptyPageSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            Date startDate = Date.from(Instant.parse("0001-01-01T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("9999-12-31T00:00:00Z"));
            int page = 1;
            int pageSize = 1;
            Pageable pageable = PageRequest.of(page,pageSize);

            User writer1 = buildAndPersistsUser("Other");
            User writer2 = buildAndPersistsUser(writerUsername);

            buildAndPersistsNews("OtherTitle",writer1,Date.from(Instant.parse("2025-11-18T12:00:00Z")));
            buildAndPersistsNews("TitleTest",writer1,Date.from(Instant.parse("2025-11-12T20:00:00Z")));
            buildAndPersistsNews("Third",writer2,Date.from(Instant.parse("2025-11-15T00:00:00Z")));

            //Act
            Page<News> newsPageReturned = newsRepository.findNews(title,writerUsername,startDate,endDate,pageable);

            //Assert
            assertTrue(newsPageReturned.isEmpty());
            assertEquals(0,newsPageReturned.getTotalElements());
            assertEquals(0,newsPageReturned.getTotalPages());
        }

        @Test
        @DisplayName("Should return empty page when the page is out of nound")
        void pageFindNewsTestPageOutOfBoundEmptyPageSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            Date startDate = Date.from(Instant.parse("2025-11-12T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-11-13T00:00:00Z"));
            int page = 2;
            int pageSize = 1;
            Pageable pageable = PageRequest.of(page,pageSize);

            User matchedWriter = buildAndPersistsUser(writerUsername);

            buildAndPersistsNews(title.concat("1"),matchedWriter,Date.from(Instant.parse("2025-11-12T14:00:00Z")));
            buildAndPersistsNews(title.concat("2"),matchedWriter,Date.from(Instant.parse("2025-11-12T12:00:00Z")));

            User unmatchedWriter = buildAndPersistsUser("WriterUnmatched");

            buildAndPersistsNews("TitleUnmatched",unmatchedWriter,Date.from(Instant.parse("2025-11-15T00:00:00Z")));
            //Act
            Page<News> newsPageReturned = newsRepository.findNews(title,writerUsername,startDate,endDate,pageable);

            //Assert
            assertTrue(newsPageReturned.isEmpty());
            assertEquals(2,newsPageReturned.getTotalElements());
            assertEquals(2,newsPageReturned.getTotalPages());
        }

        @Test
        @DisplayName("Should return a page with the correct number of news even when it's the last page")
        void pageFindNewsTestLastPageSuccess(){
            //Arrange
            Date startDate = Date.from(Instant.parse("0001-01-01T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("9999-12-31T00:00:00Z"));
            int page = 1;
            int pageSize = 2;
            Pageable pageable = PageRequest.of(page,pageSize);

            User writer = buildAndPersistsUser("WriterTest");

            News news1 = buildAndPersistsNews("TitleTest1",writer,Date.from(Instant.parse("2025-11-15T00:00:00Z")));
            News news2 = buildAndPersistsNews("TitleTest2",writer,Date.from(Instant.parse("2025-11-12T14:00:00Z")));

            User otherWriter = buildAndPersistsUser("UserOther");

            News news3 = buildAndPersistsNews("Other",otherWriter,Date.from(Instant.parse("2025-11-12T12:00:00Z")));

            //Act
            Page<News> newsPageReturned = newsRepository.findNews(null,null,startDate,endDate,pageable);

            //Assert
            assertEquals(1,newsPageReturned.getContent().size());
            assertEquals(3,newsPageReturned.getTotalElements());
            assertEquals(2,newsPageReturned.getTotalPages());

            assertThat(newsPageReturned)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .contains(news3);
            assertFalse(newsPageReturned.getContent().contains(news1));
            assertFalse(newsPageReturned.getContent().contains(news2));
        }
    }

    private User buildAndPersistsUser(String username){
        User user = UserTestFactory.buildOne(username);
        entityManager.persist(user);
        return user;
    }

    private News buildAndPersistsNews(String title, User writer, Date publicationDate){
        News news = NewsTestFactory.buildOneWithoutId(title,writer,publicationDate);
        entityManager.persist(news);
        return news;
    }

}