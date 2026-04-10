package com.Social.demo.repository;

import com.Social.demo.entity.Post;
import com.Social.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Query("SELECT p FROM Post p WHERE p.user = :user " +
            "OR p.user IN (SELECT f.following FROM Follow f WHERE f.follower = :user) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPersonalizedFeed(@Param("user") User user, Pageable pageable);
    //  Feature 4: The Trending Hashtag Algorithm (Native SQL)
    @Query(value = "SELECT word FROM (" +
            "  SELECT regexp_split_to_table(content, '\\s+') as word " +
            "  FROM posts " +
            "  WHERE created_at > NOW() - INTERVAL '1 day'" +
            ") t " +
            "WHERE word LIKE '#%'" +
            "GROUP BY word " +
            "ORDER BY COUNT(word) DESC " +
            "LIMIT 5",
            nativeQuery = true)
    List<String> findTrendingHashtags();


}