package com.Social.demo.repository;

import com.Social.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Spring Data JPA handles all the basic save() and findById() methods automatically!
}