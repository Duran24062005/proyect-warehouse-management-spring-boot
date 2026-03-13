package com.proyectS1.warehouse_management.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter { // We switched to OncePerRequestFilter

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if(header != null && header.startsWith("Bearer ")){
            String token = header.substring(7);
            String username = jwtService.validateToken(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Authentication creation
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(token, username);

                // Set the security
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        // We continue the execution
        filterChain.doFilter(request, response);
    }
    
}
