package com.Social.demo.controller;

import com.Social.demo.dto.LoginRequest;
import com.Social.demo.entity.PasswordResetToken;
import com.Social.demo.entity.User;
import com.Social.demo.repository.PasswordResetTokenRepository;
import com.Social.demo.repository.UserRepository;
import com.Social.demo.security.JwtUtil;
import com.Social.demo.service.FollowService;
import com.Social.demo.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final FollowService followService;
    private final UserRepository userRepository;

    //  NEW: Added these for password reset
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    // NEW: Updated constructor to inject everything
    public UserController(UserService userService, JwtUtil jwtUtil, FollowService followService,
                          UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.followService = followService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        User user = userService.loginUser(request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getUserProfile() {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok("Welcome to the VIP area! Your email is: " + loggedInEmail);
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<String> toggleFollow(@PathVariable Long userId){
        String result = followService.toggleFollow(userId);
        return ResponseEntity.ok(result);
    }

    // 🗑️ Delete MY profile
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyProfile() {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        return ResponseEntity.ok("Your profile and all your data have been permanently deleted.");
    }

    // NEW: Request Password Reset
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("If this email exists, a reset link has been sent."));

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        // SIMULATE SENDING AN EMAIL IN CONSOLE
        System.out.println("=================================================");
        System.out.println("EMAIL SIMULATION:");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Password Reset Request");
        System.out.println("Your password reset token is: " + token);
        System.out.println("This token expires in 15 minutes.");
        System.out.println("=================================================");

        return ResponseEntity.ok("If this email exists in our system, a password reset token has been sent.");
    }

    //  NEW: Execute Password Reset
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            return ResponseEntity.badRequest().body("Token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
        return ResponseEntity.ok("Password has been successfully reset. You can now log in.");
    }
}