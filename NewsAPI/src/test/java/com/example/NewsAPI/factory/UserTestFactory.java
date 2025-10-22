package com.example.NewsAPI.factory;

import com.example.NewsAPI.domain.user.User;
import com.example.NewsAPI.domain.user.UserRole;

public class UserTestFactory {
    public static User buildOne(){
        return new User("UserTest","123", UserRole.WRITER);
    }

    public static User buildOne(String username){
        return new User(username,"123", UserRole.WRITER);
    }
}
