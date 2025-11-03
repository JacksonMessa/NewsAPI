package com.example.NewsAPI.domain.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Payload;
import com.example.NewsAPI.domain.user.User;
import com.example.NewsAPI.exception.RetrievingHttpTokenException;
import com.example.NewsAPI.exception.TokenGenerationException;
import com.example.NewsAPI.factory.UserTestFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    TokenService tokenService;

    @Mock
    TemporalService temporalService;

    @AfterEach
    void afterEach(){
        RequestContextHolder.resetRequestAttributes();
    }

    @Nested
    class generateToken{
        @Test
        @DisplayName("Should generate a valid JWT with the expected subject, issuer and expiration time")
        void  generateTokenTestSuccess(){
            //Arrange
            User user = UserTestFactory.buildOne("TestUser");
            String secret = "TestSecret";
            ReflectionTestUtils.setField(tokenService, "secret", secret);

            int hours = 5;
            Instant expectedExpiration = Instant.now().plus(hours,ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
            when(temporalService.plusHoursFromNow(hours)).thenReturn(expectedExpiration);
            //Act
            String tokenReturned = tokenService.generateToken(user);

            //Assert
            verify(temporalService).plusHoursFromNow(hours);

            verifyNoMoreInteractions(temporalService);

            assertNotNull(tokenReturned);
            assertFalse(tokenReturned.isEmpty());

            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier jwtVerifier = JWT.require(algorithm)
                    .withIssuer("news-api")
                    .build();

            DecodedJWT jwt = jwtVerifier.verify(tokenReturned);

            assertThat(jwt)
                    .extracting(Payload::getSubject,Payload::getIssuer,Payload::getExpiresAtAsInstant)
                    .containsExactly(user.getUsername(),"news-api", expectedExpiration);
        }
    }

    @Test
    @DisplayName("Should throw TokenGenerationException if secret is null")
    void  generateTokenTestNullSecretFailure(){
        //Arrange
        User user = UserTestFactory.buildOne();
        ReflectionTestUtils.setField(tokenService, "secret", null);

        //Act / Assert
        Exception exception = assertThrows(TokenGenerationException.class,
                () -> tokenService.generateToken(user));

        //Assert
        assertEquals("Error while generating token",exception.getMessage());
    }

    @Nested
    class  validateTokenAndGetUsername{
        @DisplayName("Should return the respective username when a valid token is sent")
        @Test
        void validateTokenAndGetUsernameTestSuccess(){
            //Arrange
            User user = UserTestFactory.buildOne("TestUser");
            String secret = "TestSecret";
            ReflectionTestUtils.setField(tokenService, "secret", secret);



            int hours = 5;
            Instant expectedExpiration = Instant.now().plus(hours,ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
            when(temporalService.plusHoursFromNow(hours)).thenReturn(expectedExpiration);

            String validToken = tokenService.generateToken(user);
            //Act
            String returnedUsername = tokenService.validateTokenAndGetUsername(validToken);

            //Assert
            verify(temporalService).plusHoursFromNow(hours);

            verifyNoMoreInteractions(temporalService);

            assertEquals(user.getUsername(),returnedUsername);
        }

        @DisplayName("Should return empty string when a valid token is sent")
        @Test
        void validateTokenAndGetUsernameTestInvalidTokenFailure(){
            //Arrange
            String secret = "TestSecret";
            ReflectionTestUtils.setField(tokenService, "secret", secret);

            String invalidToken = "INVALID_TOKEN";

            //Act
            String returnedUsername = tokenService.validateTokenAndGetUsername(invalidToken);

            //Assert
            assertEquals("",returnedUsername);
        }

        @DisplayName("Should return empty string when token is expired")
        @Test
        void validateTokenAndGetUsernameTestTokenExpiredFailure(){
            //Arrange
            User user = UserTestFactory.buildOne("TestUser");
            String secret = "TestSecret";
            ReflectionTestUtils.setField(tokenService, "secret", secret);

            Instant expiredTime = Instant.now().minusSeconds(3600).truncatedTo(ChronoUnit.SECONDS);

            String expiredToken = JWT.create()
                    .withIssuer("news-api")
                    .withSubject(user.getUsername())
                    .withExpiresAt(Date.from(expiredTime))
                    .sign(Algorithm.HMAC256(secret));

            //Act
            String returnedUsername = tokenService.validateTokenAndGetUsername(expiredToken);

            //Assert
            assertEquals("",returnedUsername);
        }
    }

    @Nested
    class recoverToken{
        @Nested
        class WithHttpServletRequest{
            @Test
            @DisplayName("Should retrieve the token from the header, remove the text \"Bearer\", and return it.")
            void recoverTokenSuccess(){
                //Arrange
                HttpServletRequest request = mock(HttpServletRequest.class);

                String headerRequired = "Authorization";
                String headerExpected = "Bearer 123Token123";

                String expectedToken = "123Token123";

                when(request.getHeader(headerRequired)).thenReturn(headerExpected);

                //Act
                String returnedToken = tokenService.recoverToken(request);

                //Assert
                verify(request).getHeader(headerRequired);

                verifyNoMoreInteractions(request);

                assertEquals(expectedToken,returnedToken);
            }

            @Test
            @DisplayName("Should return null when authorization header is null")
            void recoverTokenAuthHeaderNullFailure(){
                //Arrange
                HttpServletRequest request = mock(HttpServletRequest.class);

                String headerRequired = "Authorization";

                when(request.getHeader(headerRequired)).thenReturn(null);

                //Act
                String returnedToken = tokenService.recoverToken(request);

                //Assert
                verify(request).getHeader(headerRequired);

                verifyNoMoreInteractions(request);

                assertNull(returnedToken);
            }
        }

        @Nested
        class WithoutHttpServletRequest {
            @Test
            @DisplayName("Should retrieve the HttpServeLetRequest from the thread context and pass it as a parameter to the recoverToken(request) method")
            void recoverTokenSuccess(){
                //Arrange
                ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
                HttpServletRequest httpRequest = mock(HttpServletRequest.class);

                String headerRequired = "Authorization";
                String headerExpected = "Bearer 123Token123";

                String expectedToken = "123Token123";

                RequestContextHolder.setRequestAttributes(attributes);

                when(attributes.getRequest()).thenReturn(httpRequest);
                when(httpRequest.getHeader(headerRequired)).thenReturn(headerExpected);
                //Act
                String returnedToken = tokenService.recoverToken();

                //Assert
                verify(attributes).getRequest();
                verify(httpRequest).getHeader(headerRequired);

                verifyNoMoreInteractions(attributes,httpRequest);

                assertEquals(expectedToken,returnedToken);
            }

            @Test
            @DisplayName("Should throw a RetrievingHttpTokenException when it cannot retrieve the context.")
            void recoverTokenAuthHeaderNullFailure(){
                //Act / Assert
                Exception exception = assertThrows(RetrievingHttpTokenException.class,
                        () ->tokenService.recoverToken());

                //Assert
                assertEquals("Error retrieving HttpServletRequest",exception.getMessage());
            }
        }
    }
}
