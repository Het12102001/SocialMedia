package com.Social.demo.service;

import com.Social.demo.dto.CommentRequest;
import com.Social.demo.entity.Comment;
import com.Social.demo.entity.Post;
import com.Social.demo.entity.User;
import com.Social.demo.repository.CommentRepository;
import com.Social.demo.repository.PostRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long postId, CommentRequest request) {
        // 1. Get logged-in user from JWT
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Find the post they are commenting on
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 3. Build and save the comment
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUser(user);
        comment.setPost(post);

        return commentRepository.save(comment);
    }
}