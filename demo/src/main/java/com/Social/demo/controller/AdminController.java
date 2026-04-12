package com.Social.demo.controller;

import com.Social.demo.service.UserService;
import com.Social.demo.service.PostService;
import com.Social.demo.repository.CommentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final CommentRepository commentRepository;

    public AdminController(UserService userService, PostService postService, CommentRepository commentRepository) {
        this.userService = userService;
        this.postService = postService;
        this.commentRepository = commentRepository;
    }

    // 🛡ADMIN STATS: Get total count of users in the system
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userService.countAllUsers());
        return ResponseEntity.ok(stats);
    }

    // 🛡 GOD MODE: Delete ANY user profile (Purges everything)
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.adminDeleteUser(userId);
        return ResponseEntity.ok("ADMIN ACTION: User profile and all associated data purged.");
    }

    // 🛡️ GOD MODE: Delete ANY post (Purges post image, comments, and likes)
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deleteAnyPost(@PathVariable Long postId) {
        postService.deletePost(postId); // postService already handles file deletion
        return ResponseEntity.ok("ADMIN ACTION: Post and interactions deleted successfully.");
    }

    // 🛡️ GOD MODE: Delete ANY comment
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteAnyComment(@PathVariable Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            return ResponseEntity.badRequest().body("Comment not found");
        }
        commentRepository.deleteById(commentId);
        return ResponseEntity.ok("ADMIN ACTION: Comment removed.");
    }
}