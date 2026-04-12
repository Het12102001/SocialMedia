package com.Social.demo.service;

import com.Social.demo.entity.ChatMessage;
import com.Social.demo.entity.User;
import com.Social.demo.repository.ChatMessageRepository;
import com.Social.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatMessage saveMessage(String senderEmail, Long recipientId, String content) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getHistory(String currentUserEmail, String otherUsername) {
        User user1 = userRepository.findByEmail(currentUserEmail).orElseThrow();
        User user2 = userRepository.findByUsernameIgnoreCase(otherUsername).orElseThrow();

        return chatMessageRepository.findChatHistory(user1, user2);
    }

    // Delete a single message
    @Transactional
    public void deleteMessage(Long messageId, String userEmail) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User user = userRepository.findByEmail(userEmail).orElseThrow();

        // Security: Only the sender or an Admin can delete a message
        if (!msg.getSender().getEmail().equals(userEmail) && !"ROLE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Unauthorized to delete this message");
        }

        chatMessageRepository.delete(msg);
    }

    //  Clear entire chat history
    @Transactional
    public void clearChatHistory(String currentUserEmail, String otherUsername) {
        User user1 = userRepository.findByEmail(currentUserEmail).orElseThrow();
        User user2 = userRepository.findByUsernameIgnoreCase(otherUsername).orElseThrow();

        chatMessageRepository.deleteChatHistory(user1, user2);
    }
}