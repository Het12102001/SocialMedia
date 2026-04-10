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

    public CommentController(CommentService commentService, CommentRepository commentRepository) {
        this.commentService = commentService;
        this.commentRepository = commentRepository;
    }

    // 1. ADD COMMENT (Anyone can comment on any post!)
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable("postId") Long postId, @RequestBody CommentRequest request) {
        Comment savedComment = commentService.addComment(postId, request);
        return ResponseEntity.ok(savedComment);
    }

    // 2. DELETE COMMENT (You can only delete it if YOU wrote it)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteMyComment(@PathVariable("commentId") Long commentId) {
        String loggedInIdentity = SecurityContextHolder.getContext().getAuthentication().getName();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        //THE RULE: Did you write this specific comment
        boolean isCommentAuthor = comment.getUser().getEmail().equalsIgnoreCase(loggedInIdentity) ||
                comment.getUser().getUsername().equalsIgnoreCase(loggedInIdentity);

        if (!(isCommentAuthor || isAdmin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You can only delete your own comments.");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully.");
    }
}