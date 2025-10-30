package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.user.*;
import com.example.NewsAPI.exception.IncorrectLoginCredentialsException;
import com.example.NewsAPI.exception.UserAlreadyRegisteredException;
import com.example.NewsAPI.factory.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    TokenService tokenService;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Nested
    class create {
        @Test
        @DisplayName("Should create the user with the passed parameters and encrypted password")
        public void registerTestSuccess(){
            //Arrange
            String username = "UserTest";
            String password = "123";
            String encryptedPassword = "encrypted_123";
            UserRole userRole = UserRole.WRITER;

            RegisterRequestDTO userData = new RegisterRequestDTO(username,password,UserRole.WRITER);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            when(userRepository.findByUsername(username)).thenReturn(null);
            when(bCryptPasswordEncoder.encode(password)).thenReturn(encryptedPassword);
            //Act

            userService.create(userData);

            //Assert
            verify(userRepository).findByUsername(username);
            verify(bCryptPasswordEncoder).encode(password);
            verify(userRepository).save(userCaptor.capture());

            verifyNoMoreInteractions(userRepository,bCryptPasswordEncoder);

            assertThat(userCaptor.getValue())
                    .extracting(User::getUsername,User::getPassword,User::getRole)
                    .containsExactly(username,encryptedPassword,userRole);
        }

        @Test
        @DisplayName("should return UserAlreadyRegisteredException when the submitted user is found in the UserRepository")
        public void registerTestUserAlreadyRegisteredExceptionFailure(){
            //Arrange
            String username = "UserTest";
            String password = "123";
            UserRole userRole = UserRole.WRITER;

            User userExpected = UserTestFactory.buildOne(username,userRole);

            RegisterRequestDTO userData = new RegisterRequestDTO(username,password,UserRole.WRITER);

            when(userRepository.findByUsername(username)).thenReturn(userExpected);
            //Act / Assert

            assertThrows(UserAlreadyRegisteredException.class,
                    () -> userService.create(userData));

            //Assert
            verify(userRepository).findByUsername(username);
            verify(bCryptPasswordEncoder,never()).encode(password);
            verify(userRepository,never()).save(any());

            verifyNoMoreInteractions(userRepository,bCryptPasswordEncoder);
        }
    }

    @Nested
    class login {
        @Test
        @DisplayName("Should return a predefined message, the token, and the username if the credentials are correct")
        public void loginTestSuccess(){
            //Arrange
            String username = "UserTest";
            String password = "123";
            UserRole userRole = UserRole.WRITER;
            LoginRequestDTO userData = new LoginRequestDTO(username,password);

            User userExpected = UserTestFactory.buildOne(username,userRole);
            UserResponseDTO userDTOExpected = new UserResponseDTO(username,userRole);

            Authentication authExpected = mock(Authentication.class);
            ArgumentCaptor<UsernamePasswordAuthenticationToken> usernamePasswordCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

            String expectedToken = "123Token123";


            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authExpected);
            when(authExpected.getPrincipal()).thenReturn(userExpected);
            when(tokenService.generateToken(userExpected)).thenReturn(expectedToken);
            when(userRepository.findUserByUsername(username)).thenReturn(userExpected);

            //Act

            LoginResponseDTO returnedDTO = userService.login(userData);

            //Assert

            verify(authenticationManager).authenticate(usernamePasswordCaptor.capture());
            verify(tokenService).generateToken(userExpected);
            verify(userRepository).findUserByUsername(username);

            verifyNoMoreInteractions(authenticationManager,tokenService,userRepository);

            assertThat(usernamePasswordCaptor.getValue())
                    .extracting(UsernamePasswordAuthenticationToken::getPrincipal,
                                UsernamePasswordAuthenticationToken::getCredentials)
                    .containsExactly(username,password);

            assertThat(returnedDTO)
                    .extracting(LoginResponseDTO::message,LoginResponseDTO::token,LoginResponseDTO::user)
                    .containsExactly("Login successfully",expectedToken,userDTOExpected);
        }

        @Test
        @DisplayName("Should throw an IncorrectLoginCredentialsException when the credentials are incorrect")
        public void loginTestIncorrectLoginCredentialsExceptionFailure(){
            //Arrange
            String username = "UserTest";
            String password = "1234";
            LoginRequestDTO userData = new LoginRequestDTO(username,password);

            AuthenticationException expectedException = mock(AuthenticationException.class);
            ArgumentCaptor<UsernamePasswordAuthenticationToken> usernamePasswordCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(expectedException);
            //Act / Assert

            assertThrows(IncorrectLoginCredentialsException.class,
                    ()->userService.login(userData));

            //Assert

            verify(authenticationManager).authenticate(usernamePasswordCaptor.capture());
            verify(tokenService,never()).generateToken(any(User.class));
            verify(userRepository,never()).findUserByUsername(username);

            verifyNoMoreInteractions(authenticationManager,tokenService,userRepository);

            assertThat(usernamePasswordCaptor.getValue())
                    .extracting(UsernamePasswordAuthenticationToken::getPrincipal,
                            UsernamePasswordAuthenticationToken::getCredentials)
                    .containsExactly(username,password);
        }
    }
}