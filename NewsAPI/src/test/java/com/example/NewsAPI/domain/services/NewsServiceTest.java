package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.domain.news.News;
import com.example.NewsAPI.domain.news.NewsGetResponseDTO;
import com.example.NewsAPI.domain.news.NewsGetResponseListDTO;
import com.example.NewsAPI.domain.news.NewsRequestDTO;
import com.example.NewsAPI.domain.repositories.NewsRepository;
import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.user.User;
import com.example.NewsAPI.exception.BelongsToAnotherWriterException;
import com.example.NewsAPI.exception.NewsNotFoundException;
import com.example.NewsAPI.factory.NewsTestFactory;
import com.example.NewsAPI.factory.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @InjectMocks
    NewsService newsService;

    @Mock
    NewsRepository newsRepository;

    @Mock
    TokenService tokenService;

    @Mock
    UserRepository userRepository;

    @Mock
    TemporalService dateService;

    @Mock
    Clock clock;

    @Nested
    class create{
        @Test
        @DisplayName("Should call the repository and return the news that was sent adding author and date")
        void createTestSuccess() {
            //Arrange
            User writer = UserTestFactory.buildOne();

            NewsRequestDTO newsData = new NewsRequestDTO("TitleTest","BodyTest");

            String tokenExpected = "123Token123";

            News newsExpected = NewsTestFactory.buildOne(newsData,writer);

            Date publishedAt = newsExpected.getPublishedAt();

            ArgumentCaptor<News> newsCaptor = ArgumentCaptor.forClass(News.class);

            when(clock.instant()).thenReturn(publishedAt.toInstant());
            when(tokenService.recoverToken()).thenReturn(tokenExpected);
            when(tokenService.validateTokenAndGetUsername(tokenExpected)).thenReturn(writer.getUsername());
            when(userRepository.findByUsername(writer.getUsername())).thenReturn(writer);
            when(newsRepository.save(any())).thenReturn(newsExpected);


            //Act
            News newsReturned = newsService.create(newsData);

            //Assert
            verify(clock).instant();
            verify(tokenService).recoverToken();
            verify(tokenService).validateTokenAndGetUsername(tokenExpected);
            verify(userRepository).findByUsername(writer.getUsername());
            verify(newsRepository).save(newsCaptor.capture());

            verifyNoMoreInteractions(clock, tokenService, userRepository, newsRepository);

            News newsCaptured = newsCaptor.getValue();
            assertThat(newsCaptured)
                    .extracting(News::getTitle,News::getBody,News::getPublishedAt,News::getWriter)
                    .containsExactly(newsData.title(),newsData.body(),publishedAt,writer);

            assertThat(newsReturned)
                    .extracting(News::getId,News::getTitle,News::getBody,News::getPublishedAt,News::getWriter)
                    .containsExactly(newsExpected.getId(),newsData.title(),newsData.body(),publishedAt,writer);
        }
    }

    @Nested
    class get{
        @Test
        @DisplayName("Should call the repository and return list containing 2 news with the filters sent")
        void getTestSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "UserTest";
            String publicationDate = "2025/10/14";

            Date startDate = Date.from(Instant.parse("2025-10-14T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-10-15T00:00:00Z"));

            User user = UserTestFactory.buildOne(writerUsername);

            News news1 = NewsTestFactory.buildOne(title,user,Date.from(Instant.parse("2025-10-14T12:00:00Z")));
            News news2 = NewsTestFactory.buildOne(title+"2",user,Date.from(Instant.parse("2025-10-14T11:00:00Z")));

            List<News> newsListExpected = List.of(
                news1,
                news2
            );

            List<NewsGetResponseDTO> newsListDTOExpected = NewsTestFactory.buildGetDTOList(newsListExpected);

            when(dateService.definesStartDate(publicationDate)).thenReturn(startDate);
            when(dateService.definesEndDate(publicationDate,startDate)).thenReturn(endDate);
            when(newsRepository.findNews(title,writerUsername,startDate,endDate)).thenReturn(newsListExpected);

            //Act

            List<NewsGetResponseDTO> newsListDTOReturned = newsService.get(title,writerUsername,publicationDate);

            //Assert

            verify(dateService).definesStartDate(publicationDate);
            verify(dateService).definesEndDate(publicationDate,startDate);
            verify(newsRepository).findNews(title,writerUsername,startDate,endDate);

            verifyNoMoreInteractions(dateService,newsRepository);

            assertThat(newsListDTOReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsListDTOExpected);
        }

        @Test
        @DisplayName("Should call the repository and return empty list")
        void getTestEmptyListSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "UserTest";
            String publicationDate = "2025/10/14";

            Date startDate = Date.from(Instant.parse("2025-10-14T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-10-15T00:00:00Z"));

            List<News> newsListExpected = List.of();

            when(dateService.definesStartDate(publicationDate)).thenReturn(startDate);
            when(dateService.definesEndDate(publicationDate,startDate)).thenReturn(endDate);
            when(newsRepository.findNews(title,writerUsername,startDate,endDate)).thenReturn(newsListExpected);

            //Act

            List<NewsGetResponseDTO> newsListDTOReturned = newsService.get(title,writerUsername,publicationDate);

            //Assert

            verify(dateService).definesStartDate(publicationDate);
            verify(dateService).definesEndDate(publicationDate,startDate);
            verify(newsRepository).findNews(title,writerUsername,startDate,endDate);

            verifyNoMoreInteractions(dateService,newsRepository);

            assertEquals(0, newsListDTOReturned.size());
        }

        @Test
        @DisplayName("Should call the repository and return all news when parameters are null")
        void getTestNullParamsSuccess(){
            //Arrange
            Date startDate = Date.from(Instant.parse("0001-01-01T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("9999-12-31T00:00:00Z"));

            News news1 = NewsTestFactory.buildOne("TitleTest");
            News news2 = NewsTestFactory.buildOne("TitleTest2");

            List<News> newsListExpected = List.of(
                    news1,
                    news2
            );

            List<NewsGetResponseDTO> newsListDTOExpected = NewsTestFactory.buildGetDTOList(newsListExpected);

            when(dateService.definesStartDate(null)).thenReturn(startDate);
            when(dateService.definesEndDate(null,startDate)).thenReturn(endDate);
            when(newsRepository.findNews(null,null,startDate,endDate)).thenReturn(newsListExpected);

            //Act

            List<NewsGetResponseDTO> newsListDTOReturned = newsService.get(null,null,null);

            //Assert

            verify(dateService).definesStartDate(null);
            verify(dateService).definesEndDate(null,startDate);
            verify(newsRepository).findNews(null,null,startDate,endDate);

            verifyNoMoreInteractions(dateService,newsRepository);

            assertThat(newsListDTOReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsListDTOExpected);
        }
    }

    @Nested
    class getNewsPaged{
        @DisplayName("Should call the repository and return a page containing 2 news with the filters sent")
        @Test
        void getNewsPagedTestSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "UserTest";
            String publicationDate = "2025/10/14";

            int page = 1;
            int pageSize = 2;
            Pageable pageable = PageRequest.of(page, pageSize);
            long totalElements = 10L;

            Date startDate = Date.from(Instant.parse("2025-10-14T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-10-15T00:00:00Z"));

            User user = UserTestFactory.buildOne(writerUsername);

            News news1 = NewsTestFactory.buildOne(title,user,Date.from(Instant.parse("2025-10-14T12:00:00Z")));
            News news2 = NewsTestFactory.buildOne(title+"2",user,Date.from(Instant.parse("2025-10-14T11:00:00Z")));


            List<News> newsListExpected = List.of(news1,news2);
            Page<News> newsPageExpected = new PageImpl<>(newsListExpected,pageable,totalElements);

            List<NewsGetResponseDTO> newsListDTOExpected = NewsTestFactory.buildGetDTOList(newsListExpected);

            NewsGetResponseListDTO newsResponseDTOExpected = new NewsGetResponseListDTO(
                    "News returned successfully",
                    totalElements,
                    newsPageExpected.getTotalPages(),
                    newsListDTOExpected
            );


            when(dateService.definesStartDate(publicationDate)).thenReturn(startDate);
            when(dateService.definesEndDate(publicationDate,startDate)).thenReturn(endDate);
            when(newsRepository.findNews(title,writerUsername,startDate,endDate,pageable)).thenReturn(newsPageExpected);

            //Act

            NewsGetResponseListDTO newsListDTOReturned = newsService.getNewsPaged(title,writerUsername,publicationDate,page,pageSize);

            //Assert

            verify(dateService).definesStartDate(publicationDate);
            verify(dateService).definesEndDate(publicationDate,startDate);
            verify(newsRepository).findNews(title,writerUsername,startDate,endDate,pageable);

            verifyNoMoreInteractions(dateService,newsRepository);

            assertThat(newsListDTOReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsResponseDTOExpected);
        }

        @DisplayName("Should call the repository and return a page containing 1 news, because it's the last page")
        @Test
        void getNewsPagedLastPageSizeTestSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "UserTest";
            String publicationDate = "2025/10/14";

            int page = 4;
            int pageSize = 2;
            Pageable pageable = PageRequest.of(page, pageSize);
            long totalElements = 9L;

            Date startDate = Date.from(Instant.parse("2025-10-14T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-10-15T00:00:00Z"));

            User user = UserTestFactory.buildOne(writerUsername);

            News news1 = NewsTestFactory.buildOne(title,user,Date.from(Instant.parse("2025-10-14T12:00:00Z")));


            List<News> newsListExpected = List.of(news1);
            Page<News> newsPageExpected = new PageImpl<>(newsListExpected,pageable,totalElements);

            List<NewsGetResponseDTO> newsListDTOExpected = NewsTestFactory.buildGetDTOList(newsListExpected);

            NewsGetResponseListDTO newsResponseDTOExpected = new NewsGetResponseListDTO(
                    "News returned successfully",
                    totalElements,
                    newsPageExpected.getTotalPages(),
                    newsListDTOExpected
            );


            when(dateService.definesStartDate(publicationDate)).thenReturn(startDate);
            when(dateService.definesEndDate(publicationDate,startDate)).thenReturn(endDate);
            when(newsRepository.findNews(title,writerUsername,startDate,endDate,pageable)).thenReturn(newsPageExpected);

            //Act

            NewsGetResponseListDTO newsListDTOReturned = newsService.getNewsPaged(title,writerUsername,publicationDate,page,pageSize);

            //Assert

            verify(dateService).definesStartDate(publicationDate);
            verify(dateService).definesEndDate(publicationDate,startDate);
            verify(newsRepository).findNews(title,writerUsername,startDate,endDate,pageable);

            verifyNoMoreInteractions(dateService,newsRepository);

            assertThat(newsListDTOReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsResponseDTOExpected);
        }

        @DisplayName("Should return empty content when requested page exceeds the total number of pages")
        @Test
        void getNewsPagedEmptyPageExceedsTotalPagesSuccess(){
            //Arrange
            String title = "TitleTest";
            String writerUsername = "UserTest";
            String publicationDate = "2025/10/14";

            int page = 5;
            int pageSize = 2;
            Pageable pageable = PageRequest.of(page, pageSize);
            long totalElements = 10L;

            Date startDate = Date.from(Instant.parse("2025-10-14T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2025-10-15T00:00:00Z"));

            List<News> newsListExpected = List.of();
            Page<News> newsPageExpected = new PageImpl<>(newsListExpected,pageable,totalElements);

            List<NewsGetResponseDTO> newsListDTOExpected = NewsTestFactory.buildGetDTOList(newsListExpected);

            NewsGetResponseListDTO newsResponseDTOExpected = new NewsGetResponseListDTO(
                    "News returned successfully",
                    totalElements,
                    newsPageExpected.getTotalPages(),
                    newsListDTOExpected
            );


            when(dateService.definesStartDate(publicationDate)).thenReturn(startDate);
            when(dateService.definesEndDate(publicationDate,startDate)).thenReturn(endDate);
            when(newsRepository.findNews(title,writerUsername,startDate,endDate,pageable)).thenReturn(newsPageExpected);

            //Act

            NewsGetResponseListDTO newsListDTOReturned = newsService.getNewsPaged(title,writerUsername,publicationDate,page,pageSize);

            //Assert

            verify(dateService).definesStartDate(publicationDate);
            verify(dateService).definesEndDate(publicationDate,startDate);
            verify(newsRepository).findNews(title,writerUsername,startDate,endDate,pageable);

            verifyNoMoreInteractions(dateService,newsRepository);

            assertThat(newsListDTOReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsResponseDTOExpected);
        }

        @DisplayName("Should call the repository and return a page containing 2 news when parameters are null")
        @Test
        void getNewsPagedTestNullParamsSuccess(){
            //Arrange
            int page = 1;
            int pageSize = 2;
            Pageable pageable = PageRequest.of(page, pageSize);
            long totalElements = 10L;

            Date startDate = Date.from(Instant.parse("0001-01-01T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("9999-12-31T00:00:00Z"));

            News news1 = NewsTestFactory.buildOne("TitleTest");
            News news2 = NewsTestFactory.buildOne("TitleTest2");


            List<News> newsListExpected = List.of(news1,news2);
            Page<News> newsPageExpected = new PageImpl<>(newsListExpected,pageable,totalElements);

            List<NewsGetResponseDTO> newsListDTOExpected = NewsTestFactory.buildGetDTOList(newsListExpected);

            NewsGetResponseListDTO newsResponseDTOExpected = new NewsGetResponseListDTO(
                    "News returned successfully",
                    totalElements,
                    newsPageExpected.getTotalPages(),
                    newsListDTOExpected
            );


            when(dateService.definesStartDate(null)).thenReturn(startDate);
            when(dateService.definesEndDate(null,startDate)).thenReturn(endDate);
            when(newsRepository.findNews(null,null,startDate,endDate,pageable)).thenReturn(newsPageExpected);

            //Act

            NewsGetResponseListDTO newsListDTOReturned = newsService.getNewsPaged(null,null,null,page,pageSize);

            //Assert

            verify(dateService).definesStartDate(null);
            verify(dateService).definesEndDate(null,startDate);
            verify(newsRepository).findNews(null,null,startDate,endDate,pageable);

            verifyNoMoreInteractions(dateService,newsRepository);

            assertThat(newsListDTOReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsResponseDTOExpected);
        }
    }

    @Nested
    class getOne{

        @DisplayName("Should return a news that belongs to the sent id")
        @Test
        void getOneTestSuccess(){
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            News newsExpected = NewsTestFactory.buildOne(newsId);

            when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsExpected));

            //Act

            News newsReturned = newsService.getOne(newsId);

            //Assert

            verify(newsRepository).findById(newsId);

            verifyNoMoreInteractions(newsRepository);

            assertThat(newsReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsExpected);
        }

        @DisplayName("Should throw NewsNotFoundException when not finding a news")
        @Test
        void getOneTestNewsNotFoundFailure(){
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            when(newsRepository.findById(newsId)).thenReturn(Optional.empty());

            //Act / Assert

            NewsNotFoundException exception = assertThrows(NewsNotFoundException.class,
                    () -> newsService.getOne(newsId)
            );

            //Assert

            verify(newsRepository).findById(newsId);

            verifyNoMoreInteractions(newsRepository);

            assertEquals("News Not Found", exception.getMessage());
        }
    }

    @Nested
    class update{

        @DisplayName("Should update title and body fields when authorized and return the saved news")
        @Test
        void updateTestSuccess(){
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");
            User loggedWriter = UserTestFactory.buildOne();
            NewsRequestDTO newsData = new NewsRequestDTO("TitleTestUpdated","TitleTestUpdated");

            News oldNews = NewsTestFactory.buildOne(newsId,loggedWriter);
            News newsExpected = NewsTestFactory.buildOne(newsId,newsData,loggedWriter);

            String tokenExpected = "123token123";

            ArgumentCaptor<News> newsCaptor = ArgumentCaptor.forClass(News.class);

            when(newsRepository.findById(newsId)).thenReturn(Optional.of(oldNews));
            when(tokenService.recoverToken()).thenReturn(tokenExpected);
            when(tokenService.validateTokenAndGetUsername(tokenExpected)).thenReturn(loggedWriter.getUsername());
            when(newsRepository.save(any())).thenReturn(newsExpected);

            //Act

            News newsReturned = newsService.update(newsId,newsData);

            //Assert

            verify(newsRepository).findById(newsId);
            verify(tokenService).recoverToken();
            verify(tokenService).validateTokenAndGetUsername(tokenExpected);
            verify(newsRepository).save(newsCaptor.capture());

            verifyNoMoreInteractions(newsRepository,tokenService);

            News newsCaptured = newsCaptor.getValue();
            assertThat(newsCaptured)
                    .extracting(News::getId,News::getTitle,News::getBody,News::getPublishedAt,News::getWriter)
                    .containsExactly(newsId,newsData.title(),newsData.body(),oldNews.getPublishedAt(),oldNews.getWriter());

            assertThat(newsReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsExpected);

        }

        @DisplayName("Should ignore the null params and persist the original values corresponding to these fields")
        @Test
        void updateTestNullDataParamsSuccess(){
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");
            User loggedWriter = UserTestFactory.buildOne();
            NewsRequestDTO newsData = new NewsRequestDTO(null,null);

            News oldNews = NewsTestFactory.buildOne(newsId,loggedWriter);

            String tokenExpected = "123token123";

            ArgumentCaptor<News> newsCaptor = ArgumentCaptor.forClass(News.class);

            when(newsRepository.findById(newsId)).thenReturn(Optional.of(oldNews));
            when(tokenService.recoverToken()).thenReturn(tokenExpected);
            when(tokenService.validateTokenAndGetUsername(tokenExpected)).thenReturn(loggedWriter.getUsername());
            when(newsRepository.save(any())).thenReturn(oldNews);

            //Act

            News newsReturned = newsService.update(newsId,newsData);

            //Assert

            verify(newsRepository).findById(newsId);
            verify(tokenService).recoverToken();
            verify(tokenService).validateTokenAndGetUsername(tokenExpected);
            verify(newsRepository).save(newsCaptor.capture());

            verifyNoMoreInteractions(newsRepository,tokenService);

            News newsCaptured = newsCaptor.getValue();
            assertThat(newsCaptured)
                    .extracting(News::getId,News::getTitle,News::getBody,News::getPublishedAt,News::getWriter)
                    .containsExactly(newsId,oldNews.getTitle(),oldNews.getBody(),oldNews.getPublishedAt(),oldNews.getWriter());

            assertThat(newsReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(oldNews);
        }
    }

    @DisplayName("Should throws BelongsToAnotherWriterException when the logged in user is not the writer of the news with the provided id")
    @Test
    void updateTestBelongsToAnotherWriterFailure(){
        //Arrange
        UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

        User loggedWriter = UserTestFactory.buildOne("UserTest");
        User newsWriter = UserTestFactory.buildOne("UserTestOwner");

        NewsRequestDTO newsData = new NewsRequestDTO("TitleTestUpdated","TitleTestUpdated");

        News oldNews = NewsTestFactory.buildOne(newsId,newsWriter);

        String tokenExpected = "123token123";

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(oldNews));
        when(tokenService.recoverToken()).thenReturn(tokenExpected);
        when(tokenService.validateTokenAndGetUsername(tokenExpected)).thenReturn(loggedWriter.getUsername());

        //Act / Assert
        BelongsToAnotherWriterException exception =
                assertThrows(BelongsToAnotherWriterException.class,() ->
                newsService.update(newsId,newsData));

        //Assert
        verify(newsRepository).findById(newsId);
        verify(tokenService).recoverToken();
        verify(tokenService).validateTokenAndGetUsername(tokenExpected);
        verify(newsRepository,never()).save(any());

        verifyNoMoreInteractions(newsRepository,tokenService);

        assertEquals("You are not authorized to update this news because it belongs to another user.", exception.getMessage());
    }

    @Nested
    class delete {
        @DisplayName("Should delete the news and return it")
        @Test
        void deleteTestSuccess() {
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");
            User loggedWriter = UserTestFactory.buildOne("UserTestOwner");

            News newsExpected = NewsTestFactory.buildOne(newsId,loggedWriter);

            String tokenExpected = "123token123";

            when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsExpected));
            when(tokenService.recoverToken()).thenReturn(tokenExpected);
            when(tokenService.validateTokenAndGetUsername(tokenExpected)).thenReturn(loggedWriter.getUsername());
            doNothing().when(newsRepository).delete(newsExpected);

            //Act
            News newsReturned = newsService.delete(newsId);

            //Assert

            verify(newsRepository).findById(newsId);
            verify(tokenService).recoverToken();
            verify(tokenService).validateTokenAndGetUsername(tokenExpected);
            verify(newsRepository).delete(newsExpected);

            verifyNoMoreInteractions(newsRepository,tokenService);

            assertThat(newsReturned)
                    .usingRecursiveComparison()
                    .isEqualTo(newsExpected);
        }

        @DisplayName("Should throws BelongsToAnotherWriterException when the logged in user is not the writer of the news with the provided id")
        @Test
        void deleteTestBelongsToAnotherWriterFailure(){
            //Arrange
            UUID newsId = UUID.fromString("9a3acd51-2143-4a33-81a5-6ea065285379");

            User loggedWriter = UserTestFactory.buildOne("UserTest");
            User newsWriter = UserTestFactory.buildOne("UserTestOwner");

            News newsExpected = NewsTestFactory.buildOne(newsId,newsWriter);

            String tokenExpected = "123token123";

            when(newsRepository.findById(newsId)).thenReturn(Optional.of(newsExpected));
            when(tokenService.recoverToken()).thenReturn(tokenExpected);
            when(tokenService.validateTokenAndGetUsername(tokenExpected)).thenReturn(loggedWriter.getUsername());

            //Act / Assert
            BelongsToAnotherWriterException exception =
                    assertThrows(BelongsToAnotherWriterException.class,() ->
                            newsService.delete(newsId));

            //Assert
            verify(newsRepository).findById(newsId);
            verify(tokenService).recoverToken();
            verify(tokenService).validateTokenAndGetUsername(tokenExpected);
            verify(newsRepository,never()).delete(newsExpected);

            verifyNoMoreInteractions(newsRepository,tokenService);

            assertEquals("You are not authorized to delete this news because it belongs to another user.", exception.getMessage());
        }
    }
}

