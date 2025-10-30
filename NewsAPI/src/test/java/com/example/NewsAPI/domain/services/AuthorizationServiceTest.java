package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.domain.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @InjectMocks
    AuthorizationService authorizationService;

    @Mock
    UserRepository userRepository;

    @Nested
    class loadUserByUsername{

        @Test
        @DisplayName("Should return UserDetails when the user is found")
        void loadUserByUsernameTestSuccess(){
            //Arrange
            String username = "UserTest";
            UserDetails userDetailsExpected = mock(UserDetails.class);

            when(userRepository.findByUsername(username)).thenReturn(userDetailsExpected);

            //Act

            UserDetails userDetailsReturned = authorizationService.loadUserByUsername(username);

            //Assert
            verify(userRepository).findByUsername(username);

            verifyNoMoreInteractions(userRepository);

            assertEquals(userDetailsExpected,userDetailsReturned);
        }

        @Test
        @DisplayName("Should throws UsernameNotFoundException when the user is not found")
        void loadUserByUsernameTestUsernameNotFoundExceptionFailure(){
            //Arrange
            String username = "UserTest";

            when(userRepository.findByUsername(username)).thenReturn(null);

            //Act / Assert

            assertThrows(UsernameNotFoundException.class,
                    () -> authorizationService.loadUserByUsername(username));

            //Assert
            verify(userRepository).findByUsername(username);

            verifyNoMoreInteractions(userRepository);
        }
    }

}