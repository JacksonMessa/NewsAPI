package com.example.NewsAPI.controllers;

import com.example.NewsAPI.domain.services.UserService;
import com.example.NewsAPI.domain.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("news-api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO data){
        userService.create(data);
        return ResponseEntity.ok().body(new RegisterResponseDTO("User created successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO data){
        return ResponseEntity.ok().body(userService.login(data));
    }

}
