package com.example.mygpt.infrastructure.adapters;

import com.example.mygpt.infrastructure.dtos.MessageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GeminiAIAdapterImpl implements GeminiAIAdapter {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAIAdapterImpl.class);
    
    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final AtomicLong messageIdCounter;

    public GeminiAIAdapterImpl(WebClient.Builder webClientBuilder,
                               @Value("${gemini.api.key}") String apiKey,
                               @Value("${gemini.api.url}") String apiUrl) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.messageIdCounter = new AtomicLong(1000);
    }

    @Override
    public String sendMessage(String message) {
        try {
            String requestBody = String.format(
                    "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                    message
            );

            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractTextFromResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }
    
    @Override
    public MessageResponse generateResponse(String prompt) {
        logger.info("Génération d'une réponse avec Gemini pour le prompt: {}", prompt);
        
        try {
            String aiResponse = sendMessage(prompt);
            
            // Créer et retourner un MessageResponse
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setId(messageIdCounter.getAndIncrement());
            messageResponse.setContent(aiResponse);
            messageResponse.setUserMessage(false); // Message de l'IA
            messageResponse.setCreatedAt(LocalDateTime.now());
            
            return messageResponse;
        } catch (Exception e) {
            MessageResponse errorResponse = new MessageResponse();
            errorResponse.setId(messageIdCounter.getAndIncrement());
            errorResponse.setContent("Désolé, je n'ai pas pu générer une réponse. Erreur: " + e.getMessage());
            errorResponse.setUserMessage(false);
            errorResponse.setCreatedAt(LocalDateTime.now());
            return errorResponse;
        }
    }

    private String extractTextFromResponse(String response) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();
    }
}