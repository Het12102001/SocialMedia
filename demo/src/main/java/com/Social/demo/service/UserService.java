package com.Social.demo.service;

import com.Social.demo.entity.User;
import com.Social.demo.repository.FollowRepository;
import com.Social.demo.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.io.IOException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final Cloudinary cloudinary;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       FollowRepository followRepository, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.followRepository = followRepository;
        this.cloudinary = cloudinary;
    }

    // 🚀 NEW ADMIN FEATURE: Count all users
    public long countAllUsers() {
        return userRepository.count();
    }

    // 🚀 NEW ADMIN FEATURE: Delete ANY user by ID (with full file cleanup)
    @Transactional
    public void adminDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        performFullCleanup(user);
    }

    // Existing self-delete logic
    @Transactional
    public void deleteUserPermanently() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        performFullCleanup(user);
    }

    // Private helper to handle the file & DB wipe
    private void performFullCleanup(User user) {
        // 1. Cleanup Profile Image
        if (user.getProfileImageUrl() != null) deleteCloudinaryFile(user.getProfileImageUrl());

        // 2. Cleanup all Post Images
        if (user.getPosts() != null) {
            user.getPosts().forEach(post -> {
                if (post.getImageUrl() != null) deleteCloudinaryFile(post.getImageUrl());
            });
        }

        // 3. Delete from DB (Cascades handle Comments/Likes/Follows)
        userRepository.delete(user);
    }

    private void deleteCloudinaryFile(String url) {
        try {
            int uploadIdx = url.indexOf("/upload/");
            if (uploadIdx == -1) return;
            String afterUpload = url.substring(uploadIdx + 8);
            if (afterUpload.startsWith("v") && afterUpload.indexOf('/') != -1)
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            int dotIdx = afterUpload.lastIndexOf('.');
            String publicId = dotIdx != -1 ? afterUpload.substring(0, dotIdx) : afterUpload;
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Could not delete from Cloudinary: " + e.getMessage());
        }
    }

    // ... [Rest of your existing login/register/profile methods remain here] ...
    public User loginUser(String email, String rawPassword) { return userRepository.findByEmail(email).orElseThrow(); }
    public User registerUser(User user) { user.setPassword(passwordEncoder.encode(user.getPassword())); return userRepository.save(user); }

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
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "socialhub/avatars", "resource_type", "image"));
            user.setProfileImageUrl((String) uploadResult.get("secure_url"));
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

    @Transactional
    public List<Map<String, Object>> getChatEligibleFriends() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        List<User> mutuals = followRepository.findMutualFollowers(currentUser.getId());

        return mutuals.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("profileImageUrl", u.getProfileImageUrl());
            return map;
        }).toList();
    }
}