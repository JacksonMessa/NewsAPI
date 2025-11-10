package com.example.NewsAPI.controllers;

import com.example.NewsAPI.domain.infra.security.SecurityConfiguration;
import com.example.NewsAPI.domain.news.News;
import com.example.NewsAPI.domain.news.NewsGetResponseDTO;
import com.example.NewsAPI.domain.news.NewsGetResponseListDTO;
import com.example.NewsAPI.domain.news.NewsRequestDTO;
import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.services.NewsService;
import com.example.NewsAPI.domain.services.TokenService;
import com.example.NewsAPI.domain.services.UserService;
import com.example.NewsAPI.domain.user.User;
import com.example.NewsAPI.domain.user.UserRole;
import com.example.NewsAPI.exception.BelongsToAnotherWriterException;
import com.example.NewsAPI.exception.DateConvertException;
import com.example.NewsAPI.exception.NewsNotFoundException;
import com.example.NewsAPI.factory.NewsTestFactory;
import com.example.NewsAPI.factory.UserTestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import  static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfiguration.class)
@WebMvcTest(NewsController.class)
class NewsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    NewsService newsService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;


    @Nested
    class create{
        @Test
        @DisplayName("Should return 201 Created when a WRITER creates a news with correct parameters")
        void createTest201Success() throws Exception{
            //Arrange
            NewsRequestDTO newsData = new NewsRequestDTO("TitleTest","BodyTest");

            News newsExpected = NewsTestFactory.buildOne(newsData);

            when(newsService.create(newsData)).thenReturn(newsExpected);

            //Act / Assert
            mockMvc.perform(post("/news-api/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newsData))
                        .with(user("UserTest").roles("WRITER"))
                        .with(csrf())
                    ).andExpectAll(
                        MockMvcResultMatchers.status().isCreated(),
                        header().string("Location", Matchers.endsWith("/news-api/news/" + newsExpected.getId().toString())),
                        jsonPath("$.id").value(newsExpected.getId().toString()),
                        jsonPath("$.title").value(newsExpected.getTitle()),
                        jsonPath("$.body").value(newsExpected.getBody())
                    );

            //Assert
            verify(newsService).create(newsData);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when a WRITER try to creates a news with blank parameters")
        void createTest400BlankParamsFailure() throws Exception{
            //Arrange
            NewsRequestDTO newsData = new NewsRequestDTO("","");

            //Act / Assert
            mockMvc.perform(post("/news-api/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newsData))
                        .with(user("UserTest").roles("WRITER"))
                        .with(csrf())
                    ).andExpectAll(
                        MockMvcResultMatchers.status().isBadRequest(),
                        jsonPath("$.title").value("The news title is a mandatory parameter"),
                        jsonPath("$.body").value("The news body is a mandatory parameter")
                    );

            //Assert
            verify(newsService,never()).create(any());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when a Reader try to creates a news")
        void createTest403ReaderUserFailure() throws Exception{
            //Arrange
            NewsRequestDTO newsData = new NewsRequestDTO("TitleTest","BodyTest");

            //Act / Assert
            mockMvc.perform(post("/news-api/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newsData))
                        .with(user("UserTest").roles("READER"))
                        .with(csrf())
                    ).andExpect(MockMvcResultMatchers.status().isForbidden());

            //Assert
            verify(newsService,never()).create(any());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when try to creates a news without authentication token")
        void createTest401UnauthorizedFailure() throws Exception{
            //Arrange
            NewsRequestDTO newsData = new NewsRequestDTO("TitleTest","BodyTest");

            //Act / Assert
            mockMvc.perform(post("/news-api/news")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newsData))
                            .with(csrf())
                    ).andExpect(MockMvcResultMatchers.status().isUnauthorized());

            //Assert
            verify(newsService,never()).create(any());
        }
    }

    @Nested
    class get{
        @DisplayName("It should return 200 OK with a list containing all news that match the filters")
        @Test
        void getTestNotPagedFilteredSuccess() throws Exception {
            //Arrange
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            String publicationDateString = "06/11/2025";
            Date publicationDate = Date.from(Instant.parse("2025-11-06T00:00:00Z"));
            Date publicationDate2 = Date.from(Instant.parse("2025-11-06T10:00:00Z"));

            final String NEWS_1_DATE_ISO = "2025-11-05T21:00:00.000-03:00";
            final String NEWS_2_DATE_ISO = "2025-11-06T07:00:00.000-03:00";

            User writer = UserTestFactory.buildOne(writerUsername);

            News news1 = NewsTestFactory.buildOne(title+"1",writer,publicationDate);
            News news2 = NewsTestFactory.buildOne(title+"2",writer,publicationDate2);

            List<News> newsListExpected = List.of(news1,news2);
            List<NewsGetResponseDTO> newsListDTOExpected = NewsTestFactory.buildGetDTOList(newsListExpected);

            when(newsService.get(title,writerUsername,publicationDateString)).thenReturn(newsListDTOExpected);

            //Act / Assert
            mockMvc.perform(get("/news-api/news")
                        .param("title",title)
                        .param("writer",writerUsername)
                        .param("publicationDate",publicationDateString)
                        .with(user("UserTest"))
                    ).andExpectAll(
                            MockMvcResultMatchers.status().isOk(),
                            jsonPath("$.message").value("News returned successfully"),
                            jsonPath("$.newsFound").value(2),
                            jsonPath("$.pagesFound").value(1),

                            jsonPath("$.news[0].id").value(news1.getId().toString()),
                            jsonPath("$.news[0].title").value(news1.getTitle()),
                            jsonPath("$.news[0].body").value(news1.getBody()),
                            jsonPath("$.news[0].publishedAt").value(NEWS_1_DATE_ISO),
                            jsonPath("$.news[0].writer").value(writerUsername),

                            jsonPath("$.news[1].id").value(news2.getId().toString()),
                            jsonPath("$.news[1].title").value(news2.getTitle()),
                            jsonPath("$.news[1].body").value(news2.getBody()),
                            jsonPath("$.news[1].publishedAt").value(NEWS_2_DATE_ISO),
                            jsonPath("$.news[1].writer").value(writerUsername)
                    );

            //Assert
            verify(newsService).get(title,writerUsername,publicationDateString);

            verifyNoMoreInteractions(newsService);
        }

        @DisplayName("Should return 200 OK with a list containing all news when filters is null")
        @Test
        void getTestNotPagedNotFilteredSuccess() throws Exception {

            Date publicationDate = Date.from(Instant.parse("2025-11-05T00:00:00Z"));
            Date publicationDate2 = Date.from(Instant.parse("2025-12-06T00:00:00Z"));

            final String NEWS_1_DATE_ISO = "2025-11-04T21:00:00.000-03:00";
            final String NEWS_2_DATE_ISO = "2025-12-05T21:00:00.000-03:00";

            //Arrange
            News news1 = NewsTestFactory.buildOne(publicationDate);
            News news2 = NewsTestFactory.buildOne(publicationDate2);

            List<News> newsListExpected = List.of(news1,news2);
            List<NewsGetResponseDTO> newsListDTOExpected = NewsTestFactory.buildGetDTOList(newsListExpected);

            when(newsService.get(null,null,null)).thenReturn(newsListDTOExpected);

            //Act / Assert
            mockMvc.perform(get("/news-api/news")
                            .with(user("UserTest"))
                    ).andExpectAll(
                            MockMvcResultMatchers.status().isOk(),
                            jsonPath("$.message").value("News returned successfully"),
                            jsonPath("$.newsFound").value(2),
                            jsonPath("$.pagesFound").value(1),

                            jsonPath("$.news[0].id").value(news1.getId().toString()),
                            jsonPath("$.news[0].title").value(news1.getTitle()),
                            jsonPath("$.news[0].body").value(news1.getBody()),
                            jsonPath("$.news[0].publishedAt").value(NEWS_1_DATE_ISO),
                            jsonPath("$.news[0].writer").value(news1.getWriter().getUsername()),

                            jsonPath("$.news[1].id").value(news2.getId().toString()),
                            jsonPath("$.news[1].title").value(news2.getTitle()),
                            jsonPath("$.news[1].body").value(news2.getBody()),
                            jsonPath("$.news[1].publishedAt").value(NEWS_2_DATE_ISO),
                            jsonPath("$.news[1].writer").value(news2.getWriter().getUsername())
                    );

            //Assert
            verify(newsService).get(null,null,null);

            verifyNoMoreInteractions(newsService);
        }

        @DisplayName("Should return 200 OK with a page containing news that match the filters")
        @Test
        void getTestPagedFilteredSuccess() throws Exception {
            //Arrange
            int page = 1;
            int pageSize = 2;
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            String publicationDateString = "06/11/2025";
            Date publicationDate = Date.from(Instant.parse("2025-11-06T00:00:00Z"));
            Date publicationDate2 = Date.from(Instant.parse("2025-11-06T10:00:00Z"));

            final String NEWS_1_DATE_ISO = "2025-11-05T21:00:00.000-03:00";
            final String NEWS_2_DATE_ISO = "2025-11-06T07:00:00.000-03:00";

            int listSize = 4;
            int pagesFound = 2;

            User writer = UserTestFactory.buildOne(writerUsername);

            News news1 = NewsTestFactory.buildOne(title+"1",writer,publicationDate);
            News news2 = NewsTestFactory.buildOne(title+"2",writer,publicationDate2);

            List<News> newsListExpected = List.of(news1,news2);
            List<NewsGetResponseDTO> newsListDTO = NewsTestFactory.buildGetDTOList(newsListExpected);
            NewsGetResponseListDTO newsListResponseDTOExpected = new NewsGetResponseListDTO(
                    "News returned successfully",
                    listSize,
                    pagesFound,
                    newsListDTO
            );

            when(newsService.getNewsPaged(title,writerUsername,publicationDateString,page,pageSize)).thenReturn(newsListResponseDTOExpected);

            //Act / Assert
            mockMvc.perform(get("/news-api/news")
                            .param("page",String.valueOf(page))
                            .param("pageSize",String.valueOf(pageSize))
                            .param("title",title)
                            .param("writer",writerUsername)
                            .param("publicationDate",publicationDateString)
                            .with(user("UserTest"))
                    ).andExpectAll(
                            MockMvcResultMatchers.status().isOk(),
                            jsonPath("$.message").value("News returned successfully"),
                            jsonPath("$.newsFound").value(listSize),
                            jsonPath("$.pagesFound").value(pagesFound),

                            jsonPath("$.news[0].id").value(news1.getId().toString()),
                            jsonPath("$.news[0].title").value(news1.getTitle()),
                            jsonPath("$.news[0].body").value(news1.getBody()),
                            jsonPath("$.news[0].publishedAt").value(NEWS_1_DATE_ISO),
                            jsonPath("$.news[0].writer").value(writerUsername),

                            jsonPath("$.news[1].id").value(news2.getId().toString()),
                            jsonPath("$.news[1].title").value(news2.getTitle()),
                            jsonPath("$.news[1].body").value(news2.getBody()),
                            jsonPath("$.news[1].publishedAt").value(NEWS_2_DATE_ISO),
                            jsonPath("$.news[1].writer").value(writerUsername)
                    );

            //Assert
            verify(newsService).getNewsPaged(title,writerUsername,publicationDateString,page,pageSize);

            verifyNoMoreInteractions(newsService);
        }

        @DisplayName("Should return 200 OK with a news page without filters applied")
        @Test
        void getTestPagedNotFilteredSuccess() throws Exception {
            //Arrange
            int page = 1;
            int pageSize = 2;

            Date publicationDate = Date.from(Instant.parse("2025-11-05T00:00:00Z"));
            Date publicationDate2 = Date.from(Instant.parse("2025-12-06T00:00:00Z"));

            final String NEWS_1_DATE_ISO = "2025-11-04T21:00:00.000-03:00";
            final String NEWS_2_DATE_ISO = "2025-12-05T21:00:00.000-03:00";

            int listSize = 4;
            int pagesFound = 2;

            News news1 = NewsTestFactory.buildOne(publicationDate);
            News news2 = NewsTestFactory.buildOne(publicationDate2);

            List<News> newsListExpected = List.of(news1,news2);
            List<NewsGetResponseDTO> newsListDTO = NewsTestFactory.buildGetDTOList(newsListExpected);
            NewsGetResponseListDTO newsListResponseDTOExpected = new NewsGetResponseListDTO(
                    "News returned successfully",
                    listSize,
                    pagesFound,
                    newsListDTO
            );

            when(newsService.getNewsPaged(null,null,null,page,pageSize)).thenReturn(newsListResponseDTOExpected);

            //Act / Assert
            mockMvc.perform(get("/news-api/news")
                            .param("page",String.valueOf(page))
                            .param("pageSize",String.valueOf(pageSize))
                            .with(user("UserTest"))
                    ).andExpectAll(
                            MockMvcResultMatchers.status().isOk(),
                            jsonPath("$.message").value("News returned successfully"),
                            jsonPath("$.newsFound").value(listSize),
                            jsonPath("$.pagesFound").value(pagesFound),

                            jsonPath("$.news[0].id").value(news1.getId().toString()),
                            jsonPath("$.news[0].title").value(news1.getTitle()),
                            jsonPath("$.news[0].body").value(news1.getBody()),
                            jsonPath("$.news[0].publishedAt").value(NEWS_1_DATE_ISO),
                            jsonPath("$.news[0].writer").value(news1.getWriter().getUsername()),

                            jsonPath("$.news[1].id").value(news2.getId().toString()),
                            jsonPath("$.news[1].title").value(news2.getTitle()),
                            jsonPath("$.news[1].body").value(news2.getBody()),
                            jsonPath("$.news[1].publishedAt").value(NEWS_2_DATE_ISO),
                            jsonPath("$.news[1].writer").value(news2.getWriter().getUsername())
                    );

            //Assert
            verify(newsService).getNewsPaged(null,null,null,page,pageSize);

            verifyNoMoreInteractions(newsService);
        }

        @DisplayName("Should return 200 OK with an empty list when an out-of-bounds page is requested")
        @Test
        void getTestEmptyPageSuccess() throws Exception {
            //Arrange
            int page = 2;
            int pageSize = 2;
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            String publicationDateString = "06/11/2025";

            int listSize = 0;
            int pagesFound = 2;

            List<News> newsListExpected = List.of();
            List<NewsGetResponseDTO> newsListDTO = NewsTestFactory.buildGetDTOList(newsListExpected);
            NewsGetResponseListDTO newsListResponseDTOExpected = new NewsGetResponseListDTO(
                    "News returned successfully",
                    listSize,
                    pagesFound,
                    newsListDTO
            );

            when(newsService.getNewsPaged(title,writerUsername,publicationDateString,page,pageSize)).thenReturn(newsListResponseDTOExpected);

            //Act / Assert
            mockMvc.perform(get("/news-api/news")
                            .param("page",String.valueOf(page))
                            .param("pageSize",String.valueOf(pageSize))
                            .param("title",title)
                            .param("writer",writerUsername)
                            .param("publicationDate",publicationDateString)
                            .with(user("UserTest"))
                    ).andExpectAll(
                            MockMvcResultMatchers.status().isOk(),
                            jsonPath("$.message").value("News returned successfully"),
                            jsonPath("$.newsFound").value(listSize),
                            jsonPath("$.pagesFound").value(pagesFound),
                            jsonPath("$.news", empty())
                    );

            //Assert
            verify(newsService).getNewsPaged(title,writerUsername,publicationDateString,page,pageSize);

            verifyNoMoreInteractions(newsService);
        }

        @DisplayName("Should return 400 Bad Request when date with invalid format is sent")
        @Test
        void getTest400InvalidDateFormatFailure() throws Exception {
            //Arrange
            String invalidDateString = "06-11-2025";
            when(newsService.get(null,null,invalidDateString)).thenThrow(DateConvertException.class);

            //Act / Assert
            mockMvc.perform(get("/news-api/news")
                    .param("publicationDate",invalidDateString)
                    .with(user("UserTest"))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isBadRequest(),
                    MockMvcResultMatchers.content().string("Error converting data. This parameter must be sent in DD/MM/YYYY format.")
            );

            //Assert
            verify(newsService).get(null,null,invalidDateString);

            verifyNoMoreInteractions(newsService);
        }



        @DisplayName("Should return 401 Unauthorized when try to get news without authentication token")
        @Test
        void getTest401UnauthorizedFailure() throws Exception {
            //Arrange
            int page = 1;
            int pageSize = 2;
            String title = "TitleTest";
            String writerUsername = "WriterTest";
            String publicationDateString = "06/11/2025";

            //Act / Assert
            mockMvc.perform(get("/news-api/news")
                            .param("page",String.valueOf(page))
                            .param("pageSize",String.valueOf(pageSize))
                            .param("title",title)
                            .param("writer",writerUsername)
                            .param("publicationDate",publicationDateString)
                    ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
            //Assert

            verify(newsService,never()).get(any(),any(),any());
            verify(newsService,never()).getNewsPaged(anyString(),anyString(),anyString(),anyInt(),anyInt());
        }
    }

    @Nested class getOne{
        @Test
        @DisplayName("Should return the news with the provided ID")
        void getOneTestSuccess() throws Exception {
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            Date publicationDate = Date.from(Instant.parse("2025-11-05T00:00:00Z"));
            final String NEWS_DATE_ISO = "2025-11-04T21:00:00.000-03:00";

            News newsExpected = NewsTestFactory.buildOne(newsId,publicationDate);

            when(newsService.getOne(newsId)).thenReturn(newsExpected);

            //Act / Assert
            mockMvc.perform(get("/news-api/news/{newsId}",newsId)
                    .with(user("UserTest"))
                ).andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        jsonPath("$.message").value("News found successfully"),
                        jsonPath("$.title").value(newsExpected.getTitle()),
                        jsonPath("$.body").value(newsExpected.getBody()),
                        jsonPath("$.publishedAt").value(NEWS_DATE_ISO),
                        jsonPath("$.writer").value(newsExpected.getWriter().getUsername())

                );

            //Assert
            verify(newsService).getOne(newsId);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 404 Not Found when News Service throws NewsNotFoundException")
        void getOneTest404NotFoundFailure() throws Exception {
            //Arrange
            UUID incorrectID = UUID.randomUUID();

            when(newsService.getOne(incorrectID)).thenThrow(new NewsNotFoundException("News Not Found"));

            //Act / Assert
            mockMvc.perform(get("/news-api/news/{newsId}",incorrectID)
                    .with(user("UserTest"))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isNotFound(),
                    MockMvcResultMatchers.content().string("No news item was found with the provided ID")
            );

            //Assert
            verify(newsService).getOne(incorrectID);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when an ID with an incorrect type was sent")
        void getOneTest400IncorrectTypeFailure() throws Exception {
            //Act / Assert
            mockMvc.perform(get("/news-api/news/{newsId}",1)
                    .with(user("UserTest"))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isBadRequest(),
                    MockMvcResultMatchers.content().string("Some of the parameters sent contain the wrong type.")
            );

            //Assert
            verify(newsService,never()).getOne(any());

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 401 Unauthorized try to get news without authentication token")
        void getOneTest401UnauthorizedFailure() throws Exception {
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            //Act / Assert
            mockMvc.perform(get("/news-api/news/{newsId}",newsId)
            ).andExpect(MockMvcResultMatchers.status().isUnauthorized());

            //Assert
            verify(newsService,never()).getOne(any());
        }
    }

    @Nested class update{
        @Test
        @DisplayName("Should update the news with the provided ID")
        void updateTestSuccess() throws Exception {
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            NewsRequestDTO newsData = new NewsRequestDTO("TitleTestUpdated","BodyTestUpdated");

            Date publicationDate = Date.from(Instant.parse("2025-11-05T00:00:00Z"));
            final String NEWS_DATE_ISO = "2025-11-04T21:00:00.000-03:00";

            User writer = UserTestFactory.buildOne("WriterTest",UserRole.WRITER);

            News newsExpected = NewsTestFactory.buildOne(newsId,newsData,writer,publicationDate);

            when(newsService.update(newsId,newsData)).thenReturn(newsExpected);

            //Act / Assert
            mockMvc.perform(put("/news-api/news/{newsId}",newsId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newsData))
                    .with(user(writer.getUsername()).roles(writer.getRole().toString()))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isOk(),
                    jsonPath("$.message").value("News updated successfully"),
                    jsonPath("$.title").value(newsExpected.getTitle()),
                    jsonPath("$.body").value(newsExpected.getBody()),
                    jsonPath("$.publishedAt").value(NEWS_DATE_ISO),
                    jsonPath("$.writer").value(newsExpected.getWriter().getUsername())
            );

            //Assert
            verify(newsService).update(newsId,newsData);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should ignore blank fields and return the news without updating them.")
        void updateTestBlankParamsSuccess() throws Exception {
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            NewsRequestDTO oldNewsData = new NewsRequestDTO("TitleTest","BodyTest");
            NewsRequestDTO newsDataSent = new NewsRequestDTO("","");

            Date publicationDate = Date.from(Instant.parse("2025-11-05T00:00:00Z"));
            final String NEWS_DATE_ISO = "2025-11-04T21:00:00.000-03:00";

            User writer = UserTestFactory.buildOne("WriterTest",UserRole.WRITER);

            News newsExpected = NewsTestFactory.buildOne(newsId,oldNewsData,writer,publicationDate);

            when(newsService.update(newsId,newsDataSent)).thenReturn(newsExpected);

            //Act / Assert
            mockMvc.perform(put("/news-api/news/{newsId}",newsId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newsDataSent))
                    .with(user(writer.getUsername()).roles(writer.getRole().toString()))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isOk(),
                    jsonPath("$.message").value("News updated successfully"),
                    jsonPath("$.title").value(newsExpected.getTitle()),
                    jsonPath("$.body").value(newsExpected.getBody()),
                    jsonPath("$.publishedAt").value(NEWS_DATE_ISO),
                    jsonPath("$.writer").value(newsExpected.getWriter().getUsername())
            );

            //Assert
            verify(newsService).update(newsId,newsDataSent);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 404 Not Found when News Service throws NewsNotFoundException")
        void updateTest404NotFoundFailure() throws Exception {
            //Arrange
            UUID incorrectId = UUID.randomUUID();

            NewsRequestDTO newsData = new NewsRequestDTO("TitleTestUpdated","BodyTestUpdated");

            User writer = UserTestFactory.buildOne("WriterTest",UserRole.WRITER);

            when(newsService.update(incorrectId,newsData)).thenThrow(new NewsNotFoundException("News Not Found"));

            //Act / Assert
            mockMvc.perform(put("/news-api/news/{newsId}",incorrectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newsData))
                    .with(user(writer.getUsername()).roles(writer.getRole().toString()))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isNotFound(),
                    MockMvcResultMatchers.content().string("No news item was found with the provided ID")
            );

            //Assert
            verify(newsService).update(incorrectId,newsData);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 403 Forbidden when the news belongs to another writer")
        void updateTest403BelongsToAnotherWriterFailure() throws Exception{
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");
            NewsRequestDTO newsData = new NewsRequestDTO("TitleTestUpdated","BodyTestUpdated");

            when(newsService.update(newsId,newsData)).thenThrow(new BelongsToAnotherWriterException("You are not authorized to update this news because it belongs to another user."));
            //Act / Assert
            mockMvc.perform(put("/news-api/news/{newsId}",newsId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newsData))
                    .with(user("NotOwner").roles("WRITER"))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isForbidden(),
                    MockMvcResultMatchers.content().string("You are not authorized to update this news because it belongs to another user.")
            );

            //Assert
            verify(newsService).update(newsId,newsData);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when an ID with an incorrect type was sent")
        void updateTest400IncorrectTypeFailure() throws Exception {
            //Arrange
            NewsRequestDTO newsData = new NewsRequestDTO("TitleTestUpdated","BodyTestUpdated");

            //Act / Assert
            mockMvc.perform(put("/news-api/news/{newsId}",1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newsData))
                    .with(user("UserTest").roles("WRITER"))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isBadRequest(),
                    MockMvcResultMatchers.content().string("Some of the parameters sent contain the wrong type.")
            );

            //Assert
            verify(newsService,never()).update(any(),any());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when a Reader try to update a news")
        void updateTest403ReaderUserFailure() throws Exception{
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            NewsRequestDTO newsData = new NewsRequestDTO("TitleTestUpdated","BodyTestUpdated");

            //Act / Assert
            mockMvc.perform(put("/news-api/news/{newsId}",newsId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newsData))
                    .with(user("UserTest").roles("READER"))
                    .with(csrf())
            ).andExpect(MockMvcResultMatchers.status().isForbidden());

            //Assert
            verify(newsService,never()).update(any(),any());
        }

        @Test
        @DisplayName("should return 401 Unauthorized try to update news without authentication token")
        void updateTest401UnauthorizedFailure() throws Exception{
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            NewsRequestDTO newsData = new NewsRequestDTO("TitleTestUpdated","BodyTestUpdated");

            //Act / Assert
            mockMvc.perform(put("/news-api/news/{newsId}",newsId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newsData))
                    .with(csrf())
            ).andExpect(MockMvcResultMatchers.status().isUnauthorized());

            //Assert
            verify(newsService,never()).update(any(),any());
        }
    }

    @Nested
    class delete {
        @Test
        @DisplayName("Should delete the news with the provided ID")
        void deleteTestSuccess() throws Exception {
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            Date publicationDate = Date.from(Instant.parse("2025-11-05T00:00:00Z"));
            final String NEWS_DATE_ISO = "2025-11-04T21:00:00.000-03:00";

            User writer = UserTestFactory.buildOne("WriterTest", UserRole.WRITER);

            News newsExpected = NewsTestFactory.buildOne(newsId,publicationDate);

            when(newsService.delete(newsId)).thenReturn(newsExpected);

            //Act / Assert
            mockMvc.perform(delete("/news-api/news/{newsId}", newsId)
                    .with(user(writer.getUsername()).roles(writer.getRole().toString()))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isOk(),
                    jsonPath("$.message").value("News deleted successfully"),
                    jsonPath("$.title").value(newsExpected.getTitle()),
                    jsonPath("$.body").value(newsExpected.getBody()),
                    jsonPath("$.publishedAt").value(NEWS_DATE_ISO),
                    jsonPath("$.writer").value(newsExpected.getWriter().getUsername())
            );

            //Assert
            verify(newsService).delete(newsId);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 404 Not Found when News Service throws NewsNotFoundException")
        void deleteTest404NotFoundFailure() throws Exception {
            //Arrange
            UUID newsId = UUID.randomUUID();

            User writer = UserTestFactory.buildOne("WriterTest", UserRole.WRITER);

            when(newsService.delete(newsId)).thenThrow(new NewsNotFoundException("News Not Found"));

            //Act / Assert
            mockMvc.perform(delete("/news-api/news/{newsId}", newsId)
                    .with(user(writer.getUsername()).roles(writer.getRole().toString()))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isNotFound(),
                    MockMvcResultMatchers.content().string("No news item was found with the provided ID")
            );

            //Assert
            verify(newsService).delete(newsId);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 403 Forbidden when the news belongs to another writer")
        void deleteTest403BelongsToAnotherWriterFailure() throws Exception{
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            when(newsService.delete(newsId)).thenThrow(new BelongsToAnotherWriterException("You are not authorized to delete this news because it belongs to another user."));
            //Act / Assert
            mockMvc.perform(delete("/news-api/news/{newsId}", newsId)
                    .with(user("NotOwner").roles("WRITER"))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isForbidden(),
                    MockMvcResultMatchers.content().string("You are not authorized to delete this news because it belongs to another user.")
            );

            //Assert
            verify(newsService).delete(newsId);

            verifyNoMoreInteractions(newsService);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when an ID with an incorrect type was sent")
        void deleteTest400IncorrectTypeFailure() throws Exception {
            //Act / Assert
            mockMvc.perform(delete("/news-api/news/{newsId}", 1)
                    .with(user("UserTest").roles("WRITER"))
                    .with(csrf())
            ).andExpectAll(
                    MockMvcResultMatchers.status().isBadRequest(),
                    MockMvcResultMatchers.content().string("Some of the parameters sent contain the wrong type.")
            );

            //Assert
            verify(newsService,never()).delete(any());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when a Reader try to delete a news")
        void deleteTest403ReaderUserFailure() throws Exception{
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            //Act / Assert
            mockMvc.perform(delete("/news-api/news/{newsId}",newsId)
                    .with(user("UserTest").roles("READER"))
                    .with(csrf())
            ).andExpect(MockMvcResultMatchers.status().isForbidden());

            //Assert
            verify(newsService,never()).delete(any());
        }

        @Test
        @DisplayName("should return 401 Unauthorized try to update news without authentication token")
        void updateTest401UnauthorizedFailure() throws Exception{
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            //Act / Assert
            mockMvc.perform(delete("/news-api/news/{newsId}", newsId)
                    .with(csrf())
            ).andExpect(MockMvcResultMatchers.status().isUnauthorized());

            //Assert
            verify(newsService,never()).delete(any());
        }
    }


}