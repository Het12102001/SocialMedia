package com.Social.demo.controller;

import com.Social.demo.entity.Post;
import com.Social.demo.entity.User;
import com.Social.demo.service.DiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    // Constructor Injection
    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    // 🚀 Search for users by username or email
    @GetMapping("/search")
    public ResponseEntity<List<User>> search(@RequestParam String query) {
        return ResponseEntity.ok(discoveryService.searchUsers(query));
    }

    // 🚀 Get the Top 5 Trending Hashtags from recent posts
    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrending() {
        return ResponseEntity.ok(discoveryService.getTrendingTags());
    }

    // 🚀 Get suggested users based on "Friends-of-Friends" logic
    @GetMapping("/suggested")
    public ResponseEntity<List<User>> getSuggested() {
        return ResponseEntity.ok(discoveryService.getSuggestedUsers());
    }

    // 🚀 Search for posts with a specific hashtag (Personalized to your network)
    @GetMapping("/search/posts")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(discoveryService.searchPersonalizedPosts(query));
    }
}