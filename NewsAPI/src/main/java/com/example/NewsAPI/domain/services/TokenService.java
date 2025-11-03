package com.example.NewsAPI.domain.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.NewsAPI.domain.user.User;
import com.example.NewsAPI.exception.RetrievingHttpTokenException;
import com.example.NewsAPI.exception.TokenGenerationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Service
public class TokenService {
    @Value("${token.secret}")
    private String secret;

    @Autowired
    TemporalService temporalService;

    public String generateToken(User user){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return  JWT.create()
                    .withIssuer("news-api")
                    .withSubject(user.getUsername())
                    .withExpiresAt(temporalService.plusHoursFromNow(5))
                    .sign(algorithm);
        }catch (Exception exception){
            throw new TokenGenerationException("Error while generating token");
        }
    }

    public String validateTokenAndGetUsername(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("news-api")
                    .build()
                    .verify(token)
                    .getSubject();
        }catch (Exception exception){
            return "";
        }
    }

    public String recoverToken(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader==null){
            return null;
        }else {
            return authHeader.replace("Bearer ","");
        }
    }

    public String recoverToken(){
        try {
            HttpServletRequest request =
                    ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                            .getRequest();
            return recoverToken(request);
        } catch (Exception e) {
            throw new RetrievingHttpTokenException("Error retrieving HttpServletRequest");
        }
    }
}
