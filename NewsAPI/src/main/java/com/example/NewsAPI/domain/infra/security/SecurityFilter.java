package com.example.NewsAPI.domain.infra.security;

import com.example.NewsAPI.domain.repositories.UserRepository;
import com.example.NewsAPI.domain.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenService tokenService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = tokenService.recoverToken(request);
        if (token!=null) {
            try{
                String username = tokenService.validateTokenAndGetUsername(token);
                UserDetails user = userRepository.findByUsername(username);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }catch (RuntimeException e){
                response.setStatus(401);
                response.getWriter().write("Failed to authenticate: " + e);
                return;
            }

        }
        filterChain.doFilter(request,response);
    }


}
