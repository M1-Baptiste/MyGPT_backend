package com.example.mygpt.infrastructure.controllers;

import com.example.mygpt.domains.user.entities.User;
import com.example.mygpt.domains.user.repositories.UserRepository;
import com.example.mygpt.infrastructure.adapters.JwtTokenProvider;
import com.example.mygpt.infrastructure.dtos.ConversationCreateRequest;
import com.example.mygpt.infrastructure.dtos.ConversationResponse;
import com.example.mygpt.infrastructure.dtos.MessageRequest;
import com.example.mygpt.infrastructure.dtos.MessageResponse;
import com.example.mygpt.infrastructure.dtos.ShareLinkResponse;
import com.example.mygpt.infrastructure.services.ConversationService;
import com.example.mygpt.infrastructure.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ConversationController {

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Autowired
    public ConversationController(ConversationService conversationService,
                                 MessageService messageService,
                                 JwtTokenProvider jwtTokenProvider,
                                 UserRepository userRepository) {
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getConversations(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // Pour le débogage, utiliser un utilisateur par défaut si le token est absent ou invalide
            String email;
            try {
                email = getUserEmailFromToken(token);
            } catch (Exception e) {
                // Utiliser le premier utilisateur trouvé dans la base de données
                User defaultUser = userRepository.findAll().stream().findFirst().orElse(null);
                if (defaultUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Collections.singletonMap("message", "Authentication required"));
                }
                email = defaultUser.getEmail();
            }
            
            List<ConversationResponse> conversations = conversationService.getUserConversations(email);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Une erreur est survenue lors de la récupération des conversations"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchConversations(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam String keyword) {
        try {
            // Même logique que getConversations
            String email;
            try {
                email = getUserEmailFromToken(token);
            } catch (Exception e) {
                User defaultUser = userRepository.findAll().stream().findFirst().orElse(null);
                if (defaultUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Collections.singletonMap("message", "Authentication required"));
                }
                email = defaultUser.getEmail();
            }
            
            List<ConversationResponse> conversations = conversationService.searchConversations(email, keyword);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Une erreur est survenue lors de la recherche de conversations"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createConversation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody ConversationCreateRequest request) {
        try {
            // Même logique que getConversations
            String email;
            try {
                email = getUserEmailFromToken(token);
            } catch (Exception e) {
                User defaultUser = userRepository.findAll().stream().findFirst().orElse(null);
                if (defaultUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Collections.singletonMap("message", "Authentication required"));
                }
                email = defaultUser.getEmail();
            }
            
            ConversationResponse conversation = conversationService.createConversation(email, request);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Une erreur est survenue lors de la création d'une conversation"));
        }
    }

    @GetMapping("/{conversationId}/share")
    public ResponseEntity<?> shareConversation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long conversationId) {
        try {
            // Même logique que getConversations
            String email;
            try {
                email = getUserEmailFromToken(token);
            } catch (Exception e) {
                User defaultUser = userRepository.findAll().stream().findFirst().orElse(null);
                if (defaultUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Collections.singletonMap("message", "Authentication required"));
                }
                email = defaultUser.getEmail();
            }
            
            ShareLinkResponse response = conversationService.generateShareLink(email, conversationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Une erreur est survenue lors de la génération du lien de partage"));
        }
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getConversation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long conversationId) {
        try {
            // Même logique que getConversations pour l'authentification
            String email;
            try {
                email = getUserEmailFromToken(token);
            } catch (Exception e) {
                User defaultUser = userRepository.findAll().stream().findFirst().orElse(null);
                if (defaultUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Collections.singletonMap("message", "Authentication required"));
                }
                email = defaultUser.getEmail();
            }
            
            ConversationResponse conversation = conversationService.getConversationWithMessages(email, conversationId);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Une erreur est survenue lors de la récupération de la conversation"));
        }
    }
    
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<?> addMessage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long conversationId,
            @RequestBody MessageRequest request) {
        try {
            String email = getUserEmailFromToken(token);
            List<MessageResponse> messages = messageService.createMessage(email, conversationId, request);
            
            // Retourner tous les messages (utilisateur et IA)
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Une erreur est survenue lors de l'ajout du message"));
        }
    }
    
    @PutMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<?> updateMessage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @RequestBody MessageRequest request) {
        try {
            String email = getUserEmailFromToken(token);
            List<MessageResponse> messages = messageService.updateMessage(email, conversationId, messageId, request);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Une erreur est survenue lors de la mise à jour du message"));
        }
    }
    
    @GetMapping("/{conversationId}/messages/search")
    public ResponseEntity<?> searchMessages(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long conversationId,
            @RequestParam String keyword) {
        try {
            String email = getUserEmailFromToken(token);
            List<MessageResponse> messages = messageService.searchMessages(email, conversationId, keyword);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Une erreur est survenue lors de la recherche de messages"));
        }
    }

    private String getUserEmailFromToken(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            try {
                return jwtTokenProvider.getEmailFromToken(token);
            } catch (Exception e) {
                throw e;
            }
        }
        throw new IllegalArgumentException("Invalid token: " + tokenHeader);
    }
} 