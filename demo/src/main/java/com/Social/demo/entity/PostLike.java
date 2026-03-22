package com.Social.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt = LocalDateTime.now();

    // MANY likes belong to ONE user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // MANY likes belong to ONE post
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}