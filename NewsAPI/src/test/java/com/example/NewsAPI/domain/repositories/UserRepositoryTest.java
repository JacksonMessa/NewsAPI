package com.example.NewsAPI.domain.repositories;

import com.example.NewsAPI.domain.user.User;
import com.example.NewsAPI.domain.user.UserRole;
import com.example.NewsAPI.factory.UserTestFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Nested
    class findByUsername{
        @Test
        @DisplayName("Should return UserDetails by existing username")
        void findByUsernameTestSuccess(){
            //Arrange
            String username = "UserTest";
            User userExpected = UserTestFactory.buildOne(username, UserRole.WRITER);
            entityManager.persist(userExpected);

            List<GrantedAuthority> authoritiesListExpected = List.of(
                    new SimpleGrantedAuthority("ROLE_WRITER"),
                    new SimpleGrantedAuthority("ROLE_READER")
            );

            //Act
            UserDetails userReturned = userRepository.findByUsername(username);

            //Assert
            assertThat(userReturned)
                    .extracting(UserDetails::getUsername,UserDetails::getPassword)
                    .containsExactly(userExpected.getUsername(),userExpected.getPassword());
            assertEquals(2,userReturned.getAuthorities().size());
            assertTrue(userReturned.getAuthorities().containsAll(authoritiesListExpected));
        }

        @Test
        @DisplayName("Should return null when searching for a non-existent username")
        void findByUsernameTestUserNotFoundFailure(){
            //Arrange
            String username = "UserNotRegistered";

            //Act
            UserDetails userReturned = userRepository.findByUsername(username);

            //Assert
            assertNull(userReturned);
        }
    }

    @Nested
    class findUserByUsername{
        @Test
        @DisplayName("Should return user by existing username")
        void findByUsernameTestSuccess(){
            //Arrange
            String username = "UserTest";
            User userExpected = UserTestFactory.buildOne(username, UserRole.WRITER);
            entityManager.persist(userExpected);

            //Act
            User userReturned = userRepository.findUserByUsername(username);

            //Assert
            assertThat(userReturned)
                    .extracting(User::getUsername,User::getPassword,User::getRole)
                    .containsExactly(userExpected.getUsername(),userExpected.getPassword(),userExpected.getRole());
        }

        @Test
        @DisplayName("Should return null when searching for a non-existent username")
        void findByUsernameTestUserNotFoundFailure(){
            //Arrange
            String username = "UserNotRegistered";

            //Act
            User userReturned = userRepository.findUserByUsername(username);

            //Assert
            assertNull(userReturned);
        }
    }
}