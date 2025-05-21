package com.example.NewsAPI.controllers;

import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.infra.security.TokenService;
import com.example.NewsAPI.domain.user.LoginRequestDTO;
import com.example.NewsAPI.domain.user.LoginResponseDTO;
import com.example.NewsAPI.domain.user.RegisterDTO;
import com.example.NewsAPI.domain.user.User;
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
    public ResponseEntity<String> register(@RequestBody RegisterDTO data){
        if(repository.findByUsername(data.username())!=null){
            return ResponseEntity.badRequest().body("This username is already registered");
        }

        String encrypetPassword = new BCryptPasswordEncoder().encode(data.password());

        User newUser = new User(data.username(), encrypetPassword, data.role());
        repository.save(newUser);
        return ResponseEntity.ok().body("User created successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO data){
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(data.username(),data.password());
        Authentication auth = authenticationManager.authenticate(usernamePassword);

        String token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok().body(new LoginResponseDTO("Login successfully",token));
    }

}
