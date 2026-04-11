package com.Social.demo.service;

import com.Social.demo.entity.Follow;
import com.Social.demo.entity.User;
import com.Social.demo.repository.FollowRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public String toggleFollow(Long targetUserId) {
        // 1. Get logged-in user (The Follower)
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User follower = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        // 2. Get the user they want to follow (The Following)
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // 3. Prevent users from following themselves
        if (follower.getId().equals(targetUser.getId())) {
            throw new RuntimeException("You cannot follow yourself");
        }

        // 4. Check if the follow relationship already exists
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), targetUser.getId());

        if (existingFollow.isPresent()) {
            // Unfollow
            followRepository.delete(existingFollow.get());
            return "Unfollowed successfully";
        } else {
            // Follow
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(targetUser);
            followRepository.save(follow);
            return "Followed successfully";
        }
    }
}