package com.Social.demo.service;

import com.Social.demo.dto.PostRequest;
import com.Social.demo.entity.Post;
import com.Social.demo.entity.User;
import com.Social.demo.repository.PostLikeRepository;
import com.Social.demo.repository.PostRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.Social.demo.entity.PostLike;
import java.util.Optional;


import java.time.LocalDateTime;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
    }

    public Post createPost(PostRequest request) {
        // 1. Get the email of the currently logged-in user from the JWT Security Context
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find that user in the database
        User user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Create the new Post and attach the User to it
        Post post = new Post();
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setCreatedAt(LocalDateTime.now());
        post.setUser(user); // This creates the database relationship!

        // 4. Save to database
        return postRepository.save(post);
    }



    public Page<Post> getAllPosts(int pageNumber, int pageSize) {
        // Create a pagination request. We also tell it to sort by "createdAt" in DESCENDING order (newest first)
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        // JpaRepository has a built-in method for this!
        return postRepository.findAll(pageable);
    }

    public String toggleLike(Long postId) {
        // 1. Get logged-in user
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Find the post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 3. Check if the like already exists
        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            // If they already liked it, remove the like (Unlike)
            postLikeRepository.delete(existingLike.get());
            return "Post unliked successfully";
        } else {
            // If they haven't liked it, create a new like
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            newLike.setCreatedAt(LocalDateTime.now());
            postLikeRepository.save(newLike);
            return "Post liked successfully";
        }
    }
}