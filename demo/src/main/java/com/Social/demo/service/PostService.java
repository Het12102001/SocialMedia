package com.Social.demo.service;

import com.Social.demo.entity.Post;
import com.Social.demo.entity.User;
import com.Social.demo.entity.PostLike;
import com.Social.demo.repository.PostLikeRepository;
import com.Social.demo.repository.PostRepository;
import com.Social.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final String uploadDir = "uploads/";

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
    }

    // 🚀 THE FIX: Handles File deletion + DB deletion
    @CacheEvict(value = "trendingTags", allEntries = true)
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getImageUrl() != null) {
            try {
                Files.deleteIfExists(Paths.get("uploads/" + post.getImageUrl()));
            } catch (IOException e) {
                System.err.println("Could not delete file: " + post.getImageUrl());
            }
        }
        postRepository.delete(post);
    }

    @CacheEvict(value = "trendingTags", allEntries = true)
    public Post createPostWithFile(String content, MultipartFile file) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setContent(content);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
            post.setImageUrl(fileName);
        }

        return postRepository.save(post);
    }
    public Page<Post> getAllPosts(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findPersonalizedFeed(currentUser, pageable);
    }




//    public Page<Post> getAllPosts(int page, int size) {
//        // 1. Identify User
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        System.out.println("DEBUG: Fetching feed for user: " + email);
//
//        User currentUser = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found: " + email));
//
//        // 2. Setup Pagination
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//
//        // 3. Execute Personalized Query
//        Page<Post> result = postRepository.findPersonalizedFeed(currentUser, pageable);
//
//        System.out.println("DEBUG: Found " + result.getTotalElements() + " posts for this feed.");
//
//        return result;
//    }
//public Page<Post> getAllPosts(int pageNumber, int pageSize) {
//    // 1. Set up pagination
//    Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
//
//    // 2. IGNORING WHO IS LOGGED IN FOR A SECOND
//    // Just fetch EVERY single post in the database.
//    return postRepository.findAll(pageable);
//}

    public String toggleLike(Long postId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return "Unliked";
        } else {
            postLikeRepository.save(new PostLike(null, LocalDateTime.now(), user, post));
            return "Liked";
        }
    }

    // Add this inside PostService class:

    public Page<Post> getUserProfilePosts(String username, int page, int size) {
        User profileUser = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByUserOrderByCreatedAtDesc(profileUser, pageable);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Post> getPersonalizedFeed(int page, int size) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        // This uses the custom SQL @Query we wrote in the PostRepository earlier!
        return postRepository.findPersonalizedFeed(currentUser, pageable);
    }
}