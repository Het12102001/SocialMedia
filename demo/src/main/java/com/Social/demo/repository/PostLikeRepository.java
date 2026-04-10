package com.Social.demo.repository;

import com.Social.demo.entity.Post;
import com.Social.demo.entity.PostLike;
import com.Social.demo.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // Spring Data JPA automatically writes the SQL for this just by reading the method name!
    // Tell Spring how to delete likes for a specific post
    @Transactional
    void deleteByPost(Post post);


    Optional<PostLike> findByPostAndUser(Post post, User user);
}