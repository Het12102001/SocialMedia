package com.Social.demo.controller;

import com.Social.demo.dto.CommentRequest;
import com.Social.demo.entity.Comment;
import com.Social.demo.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody CommentRequest request) {
        Comment savedComment = commentService.addComment(postId, request);
        return ResponseEntity.ok(savedComment);
    }
}