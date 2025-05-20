package com.example.NewsAPI.NewsAPI.domain.controllers;

import com.example.NewsAPI.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.NewsAPI.domain.user.RegisterDTO;
import com.example.NewsAPI.NewsAPI.domain.user.User;
import com.example.NewsAPI.NewsAPI.domain.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("news-api/user")
public class UserControler {

    @Autowired
    UserRepository repository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO data){
        if(repository.findByUsername(data.username())!=null){
            return ResponseEntity.badRequest().body("This username is already registered");
        }


        User newUser = new User(data.username(), data.password(), data.role());
        repository.save(newUser);
        return ResponseEntity.ok().body("User created successfully");
    }

}
