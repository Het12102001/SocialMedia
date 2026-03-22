package com.Social.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    private LocalDateTime createdAt = LocalDateTime.now();

    // The user who wrote the comment (We WANT to see this in JSON)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The post the comment belongs to (We IGNORE this in JSON to prevent infinite loops)
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}