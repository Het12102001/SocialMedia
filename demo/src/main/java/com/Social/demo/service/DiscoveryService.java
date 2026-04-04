package com.Social.demo.service;

import com.Social.demo.entity.User;
import com.Social.demo.repository.PostRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscoveryService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public DiscoveryService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // Logic to find users
    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    // Logic to get trends
    public List<String> getTrendingTags() {
        return postRepository.findTrendingHashtags();
    }
}