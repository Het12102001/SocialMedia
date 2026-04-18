package com.Social.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class ModerationService {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isContentToxic(String content) {
        if (content == null || content.trim().isEmpty()) return false;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

            String safeContent = content.replace("\"", "\\\"").replace("\n", " ");

            String requestJson = "{"
                    + "\"contents\": [{\"parts\":[{\"text\": \"Analyze this text and reply ONLY with the single word TOXIC or CLEAN, nothing else. Text: " + safeContent + "\"}]}],"
                    + "\"safetySettings\": ["
                    + "  {\"category\": \"HARM_CATEGORY_HATE_SPEECH\", \"threshold\": \"BLOCK_NONE\"},"
                    + "  {\"category\": \"HARM_CATEGORY_HARASSMENT\", \"threshold\": \"BLOCK_NONE\"},"
                    + "  {\"category\": \"HARM_CATEGORY_SEXUALLY_EXPLICIT\", \"threshold\": \"BLOCK_NONE\"},"
                    + "  {\"category\": \"HARM_CATEGORY_DANGEROUS_CONTENT\", \"threshold\": \"BLOCK_NONE\"}"
                    + "]"
                    + "}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String responseBody = response.getBody();

            // Extract only the answer text from Gemini's JSON — avoids false positives
            // from "TOXIC" appearing in safety category names or reasoning text
            JsonNode root = objectMapper.readTree(responseBody);
            String answer = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText("")
                    .trim();

            System.out.println("🛡️ Moderation answer: [" + answer + "]");
            return answer.equalsIgnoreCase("TOXIC");

        } catch (Exception e) {
            System.out.println("⚠️ Moderation error (allowing post): " + e.getMessage());
            return false;
        }
    }
}
