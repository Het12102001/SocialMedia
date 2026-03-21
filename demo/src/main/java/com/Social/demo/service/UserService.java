package com.Social.demo.service;

import com.Social.demo.entity.User;
import com.Social.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Inject both Repository and PasswordEncoder
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User loginUser(String email,String rawPassword){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this"));

        return user;
    }

    public User registerUser(User user) {
        // 🔥 Scramble the password right before saving
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }
}