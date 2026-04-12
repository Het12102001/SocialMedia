package com.Social.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the URL the React frontend will use to connect
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for messages sent FROM client TO server (e.g., /app/chat)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for messages sent FROM server TO client (Topics/Queues)
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // This enables "Private" messaging logic (/user/queue/messages)
        registry.setUserDestinationPrefix("/user");
    }
}