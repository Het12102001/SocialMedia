package com.Social.demo.controller;

import com.Social.demo.dto.LoginRequest;
import com.Social.demo.dto.ResetPasswordRequest;
import com.Social.demo.entity.PasswordResetToken;
import com.Social.demo.entity.User;
import com.Social.demo.repository.PasswordResetTokenRepository;
import com.Social.demo.repository.UserRepository;
import com.Social.demo.security.JwtUtil;
import com.Social.demo.service.FollowService;
import com.Social.demo.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // Define the Logger for professional tracking
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final FollowService followService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

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

    // 1. SIGNUP
    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        logger.info("Registering new user: {}", user.getUsername());
        return userService.registerUser(user);
    }

    // 2. LOGIN (With the Bouncer fix)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed: User {} not found.", request.getEmail());
                    return new RuntimeException("Error: Invalid email or password!");
                });

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Wrong password for user {}.", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid email or password!");
        }

        // Generate Token
        logger.info("User {} logged in successfully.", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(token);
    }

    // 3. GET PROFILE (Protected)
    @GetMapping("/profile")
    public ResponseEntity<String> getUserProfile() {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok("Welcome to the VIP area! Your email is: " + loggedInEmail);
    }

    // 4. TOGGLE FOLLOW
    @PostMapping("/{userId}/follow")
    public ResponseEntity<String> toggleFollow(@PathVariable Long userId){
        String result = followService.toggleFollow(userId);
        return ResponseEntity.ok(result);
    }


    // 6. FORGOT PASSWORD (Request Reset)
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        logger.info("Received password reset request for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("If this email exists, a reset link has been sent."));

        // Clean up old tokens first
        tokenRepository.deleteByUser(user);
        tokenRepository.flush(); // Force the delete before the new insert

        // Create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        // LOG THE TOKEN (This is what you copy from Docker logs)
        logger.info("*************************************************");
        logger.info("EMAIL SIMULATION - PASSWORD RESET");
        logger.info("To: {}", user.getEmail());
        logger.info("Your password reset token is: {}", token);
        logger.info("This token expires in 15 minutes.");
        logger.info("*************************************************");

        return ResponseEntity.ok("If this email exists in our system, a password reset token has been sent.");
    }

    // 7. RESET PASSWORD (Execute Reset)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.info("Processing password reset with token.");

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> {
                    logger.error("Reset failed: Invalid token used.");
                    return new RuntimeException("Invalid or expired token");
                });

        if (resetToken.isExpired()) {
            logger.warn("Reset failed: Token has expired.");
            tokenRepository.delete(resetToken);
            return ResponseEntity.badRequest().body("Token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete token so it can't be used twice
        tokenRepository.delete(resetToken);
        logger.info("Password successfully reset for user: {}", user.getEmail());

        return ResponseEntity.ok("Password has been successfully reset. You can now log in.");
    }

    // React will call this to get the Profile Header info
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getProfileByUsername(@PathVariable("username") String username) {
        return ResponseEntity.ok(userService.getUserProfileData(username));
    }

    // Add the image URL here so your Feed Navbar can use it later!
    @GetMapping("/me/details")
    public ResponseEntity<?> getCurrentUserDetails() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("role", user.getRole());

        return ResponseEntity.ok(userData);
    }

    //  Endpoint to accept the Edit Profile form
    @PutMapping(value = "/profile/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateMyProfile(
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            userService.updateProfile(email, bio, file);
            return ResponseEntity.ok("Profile updated successfully!");
        } catch (IOException e) {
            logger.error("Failed to upload profile picture", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed");
        }
    }


    @GetMapping("/{username}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable("username") String username) {
        return ResponseEntity.ok(userService.getUserFollowers(username));
    }


    @GetMapping("/{username}/following")
    public ResponseEntity<?> getFollowing(@PathVariable("username") String username) {
        return ResponseEntity.ok(userService.getUserFollowing(username));
    }


    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyProfile() {
        logger.info("Deep deletion request received.");
        userService.deleteUserPermanently();
        return ResponseEntity.ok("Your account and all associated files/data have been permanently purged.");
    }
}
