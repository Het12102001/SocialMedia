package com.Social.demo.controller;

import com.Social.demo.entity.Post;
import com.Social.demo.repository.CommentRepository;
import com.Social.demo.repository.PostLikeRepository;
import com.Social.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.Social.demo.repository.PostRepository;
import org.springframework.security.core.Authentication;

import java.io.IOException;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    public PostController(PostService postService, PostRepository postRepository, CommentRepository commentRepository, PostLikeRepository postLikeRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
    }

    // 1. CREATE POST (Multipart for Image Upload)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        Post savedPost = postService.createPostWithFile(content, file);
        return ResponseEntity.ok(savedPost);
    }

    // 2. GET ALL POSTS
    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.getAllPosts(page, size));
    }

    // 3. TOGGLE LIKE
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(postService.toggleLike(postId));
    }

    // 4. DELETE POST
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deleteMyPost(@PathVariable("postId") Long postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedInIdentity = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 🚀 FIX: Check both Email and Username just in case!
        boolean isOwner = post.getUser().getEmail().equalsIgnoreCase(loggedInIdentity) ||
                post.getUser().getUsername().equalsIgnoreCase(loggedInIdentity);

        if (!(isOwner || isAdmin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Security Alert: You can only delete your own posts.");
        }

        // Delete the children manually first!
        commentRepository.deleteByPost(post);
        postLikeRepository.deleteByPost(post);

        // Now the post has zero dependencies, so the database will allow this:
        postRepository.delete(post);

        return ResponseEntity.ok("Post deleted successfully.");
    }


}