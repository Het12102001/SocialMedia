package com.Social.demo.repository;

import com.Social.demo.entity.Post;
import com.Social.demo.entity.PostLike;
import com.Social.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // Spring Data JPA automatically writes the SQL for this just by reading the method name!
    Optional<PostLike> findByPostAndUser(Post post, User user);
}