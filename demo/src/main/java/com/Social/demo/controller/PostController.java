package com.Social.demo.controller;

import com.Social.demo.dto.PostRequest;
import com.Social.demo.entity.Post;
import com.Social.demo.repository.PostRepository;
import com.Social.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;

    public PostController(PostService postService,PostRepository postRepository) {

        this.postService = postService;
        this.postRepository = postRepository;
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

    //  Delete MY post
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deleteMyPost(@PathVariable Long postId) {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // THE SECURITY CHECK: Does the logged-in email match the post owner's email?
        if (!post.getUser().getEmail().equals(loggedInEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: You can only delete your own posts.");
        }

        postRepository.delete(post);
        return ResponseEntity.ok("Post deleted successfully.");
    }


}