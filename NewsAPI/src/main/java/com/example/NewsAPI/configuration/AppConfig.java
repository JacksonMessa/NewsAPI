package com.example.NewsAPI.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    @Bean
    Clock timeZonedClock(){
        return Clock.system(ZoneId.of("-03:00"));
    }
}
