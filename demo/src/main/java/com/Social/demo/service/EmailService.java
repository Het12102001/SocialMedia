package com.Social.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String toEmail, String token) {
        // 🚀 Points to your React App
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("support@socialhub.com");
        message.setTo(toEmail);
        message.setSubject("Reset Your SocialHub Password");
        message.setText("Hello,\n\nYou requested a password reset. Click the link below to set a new password:\n\n"
                + resetUrl + "\n\n"
                + "This link will expire in 15 minutes.\n\n"
                + "If you didn't request this, ignore this email.");

        mailSender.send(message);
    }
}