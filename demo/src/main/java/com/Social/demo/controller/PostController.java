package com.Social.demo.controller;

import com.Social.demo.dto.PostRequest;
import com.Social.demo.entity.Post;
import com.Social.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        Post savedPost = postService.createPost(request);
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> posts = postService.getAllPosts(page, size);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long postId) {
        String responseMessage = postService.toggleLike(postId);
        return ResponseEntity.ok(responseMessage);
    }


}