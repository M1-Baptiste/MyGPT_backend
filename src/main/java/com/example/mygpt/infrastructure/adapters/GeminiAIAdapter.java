package com.example.mygpt.infrastructure.adapters;

import com.example.mygpt.infrastructure.dtos.MessageRequest;
import com.example.mygpt.infrastructure.dtos.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interface définissant les méthodes pour interagir avec l'API Gemini
 */
public interface GeminiAIAdapter {
    
    /**
     * Envoie un message à l'API Gemini et retourne la réponse sous forme de chaîne de caractères
     * 
     * @param message Le message à envoyer
     * @return La réponse de l'API
     */
    String sendMessage(String message);
    
    /**
     * Génère une réponse à partir d'un prompt et renvoie un objet MessageResponse
     * 
     * @param prompt Le prompt à envoyer à l'API
     * @return Un objet MessageResponse contenant la réponse générée
     */
    MessageResponse generateResponse(String prompt);
}