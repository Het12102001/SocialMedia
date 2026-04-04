package com.Social.demo.controller;

import com.Social.demo.dto.CommentRequest;
import com.Social.demo.entity.Comment;
import com.Social.demo.repository.CommentRepository;
import com.Social.demo.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;
    private final CommentRepository commentRepository;

    public CommentController(CommentService commentService,CommentRepository commentRepository) {
        this.commentService = commentService;
        this.commentRepository = commentRepository;
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody CommentRequest request) {
        Comment savedComment = commentService.addComment(postId, request);
        return ResponseEntity.ok(savedComment);
    }

    // Delete MY comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteMyComment(@PathVariable Long commentId) {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // 🔥 THE SECURITY CHECK: Does the logged-in email match the comment owner's email?
        if (!comment.getUser().getEmail().equals(loggedInEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: You can only delete your own comments.");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully.");
    }
}