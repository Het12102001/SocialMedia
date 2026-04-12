package com.Social.demo.service;

import com.Social.demo.entity.Post;
import com.Social.demo.entity.User;
import com.Social.demo.repository.PostRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiscoveryService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public DiscoveryService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // 1. Search Users by Username or Email
    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    // 2. The Smart Suggestion Algorithm (Friends-of-Friends)
    @Transactional
    public List<User> getSuggestedUsers() {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(loggedInEmail).orElseThrow();

        // Get IDs of people you ALREADY follow
        List<Long> followedUserIds = loggedInUser.getFollowing().stream()
                .map(follow -> follow.getFollowing().getId())
                .toList();

        // Try to find "Friends of Friends"
        List<User> fofSuggestions = new ArrayList<>();

        for (com.Social.demo.entity.Follow myFollow : loggedInUser.getFollowing()) {
            User myFriend = myFollow.getFollowing();

            for (com.Social.demo.entity.Follow friendsFollow : myFriend.getFollowing()) {
                User friendOfFriend = friendsFollow.getFollowing();

                // If it's not ME, and I don't ALREADY follow them...
                if (!friendOfFriend.getId().equals(loggedInUser.getId()) && !followedUserIds.contains(friendOfFriend.getId())) {
                    if(!fofSuggestions.contains(friendOfFriend)) {
                        fofSuggestions.add(friendOfFriend);
                    }
                }
            }
        }

        // If we found Friends of Friends, return up to 5 of them
        if (!fofSuggestions.isEmpty()) {
            return fofSuggestions.stream().limit(5).toList();
        }

        // FALLBACK: If you have no friends yet, return 5 random strangers you don't follow
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(loggedInUser.getId()))
                .filter(u -> !followedUserIds.contains(u.getId()))
                .limit(5)
                .toList();
    }

    // 3. Timezone-Proof Trending Hashtag Extractor
    @Cacheable(value = "trendingTags")
    public List<String> getTrendingTags() {
        // Grab the 50 most recent posts, regardless of time/timezone
        Pageable latest50 = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Post> recentPosts = postRepository.findAll(latest50).getContent();

        Map<String, Integer> tagCounts = new HashMap<>();
        Pattern pattern = Pattern.compile("#\\w+"); // Regex to find #hashtags

        for (Post post : recentPosts) {
            if (post.getContent() != null) {
                Matcher matcher = pattern.matcher(post.getContent());
                while (matcher.find()) {
                    String tag = matcher.group().toLowerCase();
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                }
            }
        }

        // Sort by most popular and return the top 5
        return tagCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    // 4. Personalized Hashtag Search (Only searches your network)
    @Transactional
    public List<Post> searchPersonalizedPosts(String keyword) {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(loggedInEmail).orElseThrow();

        return postRepository.findPersonalizedPostsByHashtag(keyword, currentUser);
    }
}