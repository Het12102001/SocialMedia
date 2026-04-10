package com.Social.demo.config;

import com.Social.demo.security.JwtFilter;
import com.Social.demo.security.RateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter; //  Inject the filter
    private  final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(JwtFilter jwtFilter,RateLimitingFilter rateLimitingFilter) {
        this.jwtFilter = jwtFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // UPGRADED: Added forgot/reset password to permitAll
                        .requestMatchers("/uploads/**","/api/users/signup", "/api/users/login", "/api/users/forgot-password", "/api/users/reset-password","/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // RESTORED: Keep your God Mode locked down!
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated() // Everything else is locked
                )
                // Add our custom filter BEFORE the standard security checks

                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, RateLimitingFilter.class);

        return http.build();
    }
}