package com.Social.demo.service;

import com.Social.demo.entity.Post;
import com.Social.demo.entity.User;
import com.Social.demo.repository.PostRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

//    public Page<Post> getAllPosts(int pageNumber, int pageSize) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        User currentUser = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
//
//
//        return postRepository.findPersonalizedFeed(currentUser.getId(), pageable);
//    }
}