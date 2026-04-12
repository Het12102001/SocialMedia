package com.Social.demo.repository;

import com.Social.demo.entity.Follow;
import com.Social.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Query("SELECT f1.following FROM Follow f1 " +
            "WHERE f1.follower.id = :userId " +
            "AND f1.following IN (SELECT f2.follower FROM Follow f2 WHERE f2.following.id = :userId)")
    java.util.List<User> findMutualFollowers(@org.springframework.data.repository.query.Param("userId") Long userId);
}