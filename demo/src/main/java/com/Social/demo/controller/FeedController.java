package com.Social.demo.controller;

import com.Social.demo.entity.Post;
import com.Social.demo.service.FeedService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

//    // GET /api/feed?page=0&size=10
//    @GetMapping
//    public ResponseEntity<Page<Post>> getMyFeed(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        Page<Post> feed = feedService.getPersonalizedFeed(page, size);
//        return ResponseEntity.ok(feed);
//    }
}