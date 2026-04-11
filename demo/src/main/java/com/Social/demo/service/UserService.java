package com.Social.demo.service;

import com.Social.demo.entity.User;
import com.Social.demo.repository.FollowRepository;
import com.Social.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;
import java.io.IOException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.followRepository = followRepository;
    }

    // 🚀 NEW: Deep Delete Account Logic
    @Transactional
    public void deleteUserPermanently() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Delete physical profile image
        if (user.getProfileImageUrl() != null) {
            deletePhysicalFile(user.getProfileImageUrl());
        }

        // 2. Delete physical post images
        if (user.getPosts() != null) {
            user.getPosts().forEach(post -> {
                if (post.getImageUrl() != null) {
                    deletePhysicalFile(post.getImageUrl());
                }
            });
        }

        // 3. Delete from DB (Cascade handles posts, comments, likes, follows)
        userRepository.delete(user);
    }

    private void deletePhysicalFile(String fileName) {
        try {
            Path path = Paths.get("uploads/" + fileName);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Could not delete file: " + fileName);
        }
    }

    // ... Keep your existing loginUser, registerUser, getUserProfileData, etc. exactly as they were
    public User loginUser(String email, String rawPassword) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public Map<String, Object> getUserProfileData(String username) {
        User profileUser = userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new RuntimeException("User not found"));
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(loggedInEmail).orElse(null);
        boolean isFollowing = false;
        if (loggedInUser != null) {
            isFollowing = followRepository.findByFollowerIdAndFollowingId(loggedInUser.getId(), profileUser.getId()).isPresent();
        }
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", profileUser.getId());
        profileData.put("username", profileUser.getUsername());
        profileData.put("email", profileUser.getEmail());
        profileData.put("bio", profileUser.getBio());
        profileData.put("profileImageUrl", profileUser.getProfileImageUrl());
        profileData.put("followersCount", profileUser.getFollowers() != null ? profileUser.getFollowers().size() : 0);
        profileData.put("followingCount", profileUser.getFollowing() != null ? profileUser.getFollowing().size() : 0);
        profileData.put("isFollowing", isFollowing);
        return profileData;
    }

    public User updateProfile(String email, String bio, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (bio != null) user.setBio(bio);
        if (file != null && !file.isEmpty()) {
            Path uploadPath = Paths.get("uploads/");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String fileName = UUID.randomUUID().toString() + "_avatar_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            user.setProfileImageUrl(fileName);
        }
        return userRepository.save(user);
    }

    @Transactional
    public List<Map<String, Object>> getUserFollowers(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow();
        return user.getFollowers().stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getFollower().getId());
            m.put("username", f.getFollower().getUsername());
            m.put("profileImageUrl", f.getFollower().getProfileImageUrl());
            return m;
        }).toList();
    }

    @Transactional
    public List<Map<String, Object>> getUserFollowing(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow();
        return user.getFollowing().stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getFollowing().getId());
            m.put("username", f.getFollowing().getUsername());
            m.put("profileImageUrl", f.getFollowing().getProfileImageUrl());
            return m;
        }).toList();
    }
}