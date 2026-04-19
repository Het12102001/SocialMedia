package com.Social.demo.controller;

import com.Social.demo.entity.ChatMessage;
import com.Social.demo.service.ChatService;
import com.Social.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, Object> payload) {
        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Long recipientId = Long.valueOf(payload.get("recipientId").toString());
        String content = (String) payload.get("content");

        // 1. Save to Database Permanently
        ChatMessage savedMsg = chatService.saveMessage(senderEmail, recipientId, content);

        // Push to recipient for real-time delivery
        messagingTemplate.convertAndSend(
                "/topic/messages/" + savedMsg.getRecipient().getUsername(),
                savedMsg
        );
        // Push to sender so their optimistic message gets replaced with the real saved one
        messagingTemplate.convertAndSend(
                "/topic/messages/" + savedMsg.getSender().getUsername(),
                savedMsg
        );

        return ResponseEntity.ok(savedMsg);
    }

    @GetMapping("/history/{otherUsername}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String otherUsername) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(chatService.getHistory(email, otherUsername));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<Map<String, Object>>> getChatFriends() {
        return ResponseEntity.ok(userService.getChatEligibleFriends());
    }

    // 🚀 NEW: API to delete one message
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        chatService.deleteMessage(messageId, email);
        return ResponseEntity.ok("Message deleted");
    }

    // 🚀 NEW: API to clear the whole chat
    @DeleteMapping("/clear/{otherUsername}")
    public ResponseEntity<String> clearChat(@PathVariable String otherUsername) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        chatService.clearChatHistory(email, otherUsername);
        return ResponseEntity.ok("Chat cleared");
    }
}