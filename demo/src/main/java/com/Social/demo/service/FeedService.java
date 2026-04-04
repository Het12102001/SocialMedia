package com.Social.demo.service;

import com.Social.demo.entity.Post;
import com.Social.demo.entity.User;
import com.Social.demo.repository.PostRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public FeedService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Page<Post> getPersonalizedFeed(int page, int size) {
        // 1. Get the email of the logged-in user from the JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Create a "Pageable" object (e.g., Page 0, Size 10)
        Pageable pageable = PageRequest.of(page, size);

        // 3. Return the filtered posts
        return postRepository.findPersonalizedFeed(currentUser, pageable);
    }
}