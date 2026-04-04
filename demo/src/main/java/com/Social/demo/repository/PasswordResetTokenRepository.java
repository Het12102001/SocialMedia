package com.Social.demo.repository;

import com.Social.demo.entity.PasswordResetToken;
import com.Social.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user); // To clean up old tokens
}