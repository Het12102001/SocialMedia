package com.Social.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RateLimitingFilter rateLimitingFilter;

    public JwtFilter(JwtUtil jwtUtil,RateLimitingFilter rateLimitingFilter) {
        this.jwtUtil = jwtUtil;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Extract the Authorization header from the request
        String authHeader = request.getHeader("Authorization");

        // 2. Check if the header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " to get the pure token

            try {
                // 3. Extract the email AND role (This will fail if the token is faked or expired)
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token); // 🔥 FIX 1: Extract the role!

                // 4. If valid, tell Spring Security "This user is authenticated!"
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 🔥 Convert the String role into a Spring Security Authority
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                    // 🔥 FIX 2: Pass the 'authorities' list into the token, NOT new ArrayList<>()
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Token is invalid, expired, or tampered with
                System.out.println("JWT ERROR: " + e.getMessage());
                e.printStackTrace(); // Keeps our debugging printout from earlier!
            }
        }

        // 5. Continue the request chain
        filterChain.doFilter(request, response);
    }
}