package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.user.*;
import com.example.NewsAPI.exception.IncorrectLoginCredentialsException;
import com.example.NewsAPI.exception.UserAlreadyRegisteredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository repository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    TokenService tokenService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    public void create(RegisterRequestDTO data){
        if(repository.findByUsername(data.username())!=null){
            throw new UserAlreadyRegisteredException("This username is already registered");
        }

        String encryptedPassword = bCryptPasswordEncoder.encode(data.password());

        User newUser = new User(data.username(), encryptedPassword, data.role());
        repository.save(newUser);
    }

    public LoginResponseDTO login(LoginRequestDTO data){
        try {
            UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());
            Authentication auth = authenticationManager.authenticate(usernamePassword);
            String token = tokenService.generateToken((User) auth.getPrincipal());
            User user = repository.findUserByUsername(data.username());
            return new LoginResponseDTO("Login successfully",token,new UserResponseDTO(user.getUsername(),user.getRole()));
        } catch (Exception e) {
            throw new IncorrectLoginCredentialsException("Incorrect username or password");
        }
    }

}
