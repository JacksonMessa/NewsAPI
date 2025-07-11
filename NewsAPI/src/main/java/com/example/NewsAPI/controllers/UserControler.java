package com.example.NewsAPI.controllers;

import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.services.TokenService;
import com.example.NewsAPI.domain.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("news-api/user")
public class UserControler {

    @Autowired
    UserRepository repository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    TokenService tokenService;


    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO data){
        if(repository.findByUsername(data.username())!=null){
            return ResponseEntity.badRequest().body(new RegisterResponseDTO("This username is already registered"));
        }

        String encrypetPassword = new BCryptPasswordEncoder().encode(data.password());

        User newUser = new User(data.username(), encrypetPassword, data.role());
        repository.save(newUser);
        return ResponseEntity.ok().body(new RegisterResponseDTO("User created successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO data){
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(data.username(),data.password());
        Authentication auth = authenticationManager.authenticate(usernamePassword);

        String token = tokenService.generateToken((User) auth.getPrincipal());

        User user = repository.findUserByUsername(data.username());

        return ResponseEntity.ok().body(new LoginResponseDTO("Login successfully",token, new UserResponseDTO(user.getUsername(),user.getRole())));
    }

}
