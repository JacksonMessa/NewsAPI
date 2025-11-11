package com.example.NewsAPI.controllers;

import com.example.NewsAPI.domain.infra.security.SecurityConfiguration;
import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.services.TokenService;
import com.example.NewsAPI.domain.services.UserService;
import com.example.NewsAPI.domain.user.*;
import com.example.NewsAPI.exception.IncorrectLoginCredentialsException;
import com.example.NewsAPI.exception.TokenGenerationException;
import com.example.NewsAPI.exception.UserAlreadyRegisteredException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @Nested
    class register{
        @Test
        @DisplayName("Should create user and return 201 Created and a success message")
        void registerTestSuccess() throws Exception {
            //Arrange
            RegisterRequestDTO userData = new RegisterRequestDTO("UserTest","12345", UserRole.WRITER);

            doNothing().when(userService).create(userData);
            //Act / Assert
            mockMvc.perform(post("/news-api/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userData))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isCreated(),
                    jsonPath("message").value("User created successfully")
            );

            //Assert
            verify(userService).create(userData);

            verifyNoMoreInteractions(userService);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when invalid role is sent")
        void registerTest400InvalidRoleFailure() throws Exception {
            //Arrange
            String invalidJson = """
                {
                    "username": "UserTest",
                    "password": "12345",
                    "role": "COSTUMER"
                }
                """;
            //Act / Assert
            mockMvc.perform(post("/news-api/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson)
            ).andExpectAll(
                    MockMvcResultMatchers.status().isBadRequest(),
                    jsonPath("role").value("The user role is a mandatory parameter and should be WRITER or READER")
            );

            //Assert
            verify(userService,never()).create(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when null parameters is sent")
        void registerTest400NullParamsFailure() throws Exception {
            //Arrange
            RegisterRequestDTO nullData = new RegisterRequestDTO(null,null, null);

            //Act / Assert
            mockMvc.perform(post("/news-api/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nullData))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isBadRequest(),
                    jsonPath("username").value("The username is a mandatory parameter"),
                    jsonPath("password").value("The password is a mandatory parameter"),
                    jsonPath("role").value("The user role is a mandatory parameter and should be WRITER or READER")
            );

            //Assert
            verify(userService,never()).create(any());
        }

        @Test
        @DisplayName("Should return 409 Conflict when username is already registered")
        void registerTest409UsernameAlreadyRegistered() throws Exception {
            //Arrange
            RegisterRequestDTO userData = new RegisterRequestDTO("UserRegistered","12345", UserRole.WRITER);

            doThrow(UserAlreadyRegisteredException.class).when(userService).create(userData);
            //Act / Assert
            mockMvc.perform(post("/news-api/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userData))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isConflict(),
                    MockMvcResultMatchers.content().string("This username is already registered.")
            );

            //Assert
            verify(userService).create(userData);

            verifyNoMoreInteractions(userService);
        }
    }

    @Nested
    class login{
        @Test
        @DisplayName("Should return token, username and role when valid credentials is sent")
        void loginTestSuccess() throws Exception{
            //Arrange
            String username = "UserTest";
            String password = "12345";

            LoginRequestDTO loginData = new LoginRequestDTO(username,password);
            String tokenExpected = "123Token123";
            UserResponseDTO userExpected = new UserResponseDTO(username,UserRole.READER);

            LoginResponseDTO responseExpected = new LoginResponseDTO("Login successfully",tokenExpected,userExpected);

            when(userService.login(loginData)).thenReturn(responseExpected);
            //Act / Assert
            mockMvc.perform(post("/news-api/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isOk(),
                    MockMvcResultMatchers.jsonPath("message").value("Login successfully"),
                    MockMvcResultMatchers.jsonPath("token").value(tokenExpected),
                    MockMvcResultMatchers.jsonPath("user.username").value(username),
                    MockMvcResultMatchers.jsonPath("user.role").value(userExpected.role().toString())
            );

            //Assert
            verify(userService).login(loginData);

            verifyNoMoreInteractions(userService);
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when invalid credentials is sent")
        void loginTest401InvalidCredentialsFailure() throws Exception{
            //Arrange
            String username = "IncorrectUser";
            String password = "IncorrectPassword";

            LoginRequestDTO loginData = new LoginRequestDTO(username,password);

            when(userService.login(loginData)).thenThrow(IncorrectLoginCredentialsException.class);
            //Act / Assert
            mockMvc.perform(post("/news-api/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isUnauthorized(),
                    MockMvcResultMatchers.content().string("Incorrect username or password.")
            );

            //Assert
            verify(userService).login(loginData);

            verifyNoMoreInteractions(userService);
        }

        @Test
        @DisplayName("Should return 400 when null parameters is sent")
        void loginTest400NullParamsFailure() throws Exception{
            //Arrange
            LoginRequestDTO nullData = new LoginRequestDTO(null,null);

            //Act / Assert
            mockMvc.perform(post("/news-api/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nullData))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isBadRequest(),
                    jsonPath("username").value("The username is a mandatory parameter"),
                    jsonPath("password").value("The password is a mandatory parameter")
            );

            //Assert
            verify(userService,never()).login(any());

            verifyNoMoreInteractions(userService);
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error when an error occur while generate token")
        void loginTest500GenerateTokenFailure() throws Exception{
            //Arrange
            String username = "UserTest";
            String password = "12345";

            LoginRequestDTO loginData = new LoginRequestDTO(username,password);

            when(userService.login(loginData)).thenThrow(TokenGenerationException.class);
            //Act / Assert
            mockMvc.perform(post("/news-api/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData))
            ).andExpectAll(
                    MockMvcResultMatchers.status().isInternalServerError(),
                    MockMvcResultMatchers.content().string("Error while generating token")
            );

            //Assert
            verify(userService).login(loginData);

            verifyNoMoreInteractions(userService);
        }
    }


}


