package com.Social.demo.controller;

import com.Social.demo.repository.CommentRepository; // Add this import
import com.Social.demo.repository.PostRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository; // Add the Comment Repo
    private final UserRepository userRepository;

    // Update constructor to inject both repositories
    public AdminController(PostRepository postRepository, CommentRepository commentRepository,UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    // 🛡 GOD MODE: Delete ANY post
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deleteAnyPost(@PathVariable Long postId) {
        if (!postRepository.existsById(postId)) {
            return ResponseEntity.badRequest().body("Post not found");
        }

        postRepository.deleteById(postId);
        return ResponseEntity.ok("ADMIN ACTION: Post deleted successfully");
    }

    //  GOD MODE: Delete ANY comment
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteAnyComment(@PathVariable Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            return ResponseEntity.badRequest().body("Comment not found");
        }

        commentRepository.deleteById(commentId);
        return ResponseEntity.ok("ADMIN ACTION: Comment deleted successfully");
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest().body("User not found");
        }

        userRepository.deleteById(userId);
        return ResponseEntity.ok("ADMIN ACTION: User profile deleted successfully");
    }
}