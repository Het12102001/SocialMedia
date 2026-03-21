package com.Social.demo.controller;

import com.Social.demo.dto.LoginRequest;
import com.Social.demo.entity.User;
import com.Social.demo.security.JwtUtil;
import com.Social.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;


    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        User user = userService.loginUser(request.getEmail(), request.getPassword());

        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(token);
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getUserProfile() {
        // Find out who is currently logged in based on their token
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok("Welcome to the VIP area! Your email is: " + loggedInEmail);
    }
}