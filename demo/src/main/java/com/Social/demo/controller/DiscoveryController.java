package com.Social.demo.controller;

import com.Social.demo.entity.User;
import com.Social.demo.service.DiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    // Search for users: GET /api/discovery/search?query=het
    @GetMapping("/search")
    public ResponseEntity<List<User>> search(@RequestParam String query) {
        return ResponseEntity.ok(discoveryService.searchUsers(query));
    }

    // Get Top 5 Trending Hashtags: GET /api/discovery/trending
    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrending() {
        return ResponseEntity.ok(discoveryService.getTrendingTags());
    }
}