package com.Social.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class ModerationService {

    // If Docker can't find the key, it will use your actual key as the default backup automatically.
    @Value("${spring.ai.google.genai.api-key:AIzaSyC1yLKoc0xNw4KVs_B5U4XNqnI_Hfmxx8Y}")
    private String apiKey;

    public boolean isContentToxic(String content) {
        if (content == null || content.trim().isEmpty()) return false;
        System.out.println("\n🛡️ STARTING MODERATION CHECK FOR: " + content);

        try {
            RestTemplate restTemplate = new RestTemplate();
            //String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            //String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            // Clean the user's text so it doesn't break the JSON structure
            String safeContent = content.replace("\"", "\\\"").replace("\n", " ");

            // We manually build the JSON and force Gemini to disable its auto-blocking
            String requestJson = "{"
                    + "\"contents\": [{\"parts\":[{\"text\": \"Analyze this text and reply ONLY with the word TOXIC or CLEAN. Text: " + safeContent + "\"}]}],"
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

            // Make the call
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String responseBody = response.getBody();

            System.out.println("🛡️ RAW GOOGLE RESPONSE: " + responseBody);

            // Just check if the word TOXIC is literally anywhere in the response string
            if (responseBody != null && responseBody.toUpperCase().contains("TOXIC")) {
                System.out.println("🚨 VERDICT: Post is TOXIC. Blocking now.");
                return true;
            }

            System.out.println("✅ VERDICT: Post is CLEAN. Allowing.");
            return false;

        } catch (Exception e) {
            System.out.println("⚠️ AI ERROR (Allowing post): " + e.getMessage());
            return false;
        }
    }
}