package com.example.mygpt.infrastructure.services;

import com.example.mygpt.domains.conversation.entities.Conversation;
import com.example.mygpt.domains.conversation.repositories.ConversationRepository;
import com.example.mygpt.domains.user.entities.User;
import com.example.mygpt.domains.user.repositories.UserRepository;
import com.example.mygpt.infrastructure.adapters.JwtTokenProvider;
import com.example.mygpt.infrastructure.dtos.ConversationCreateRequest;
import com.example.mygpt.infrastructure.dtos.ConversationResponse;
import com.example.mygpt.infrastructure.dtos.ShareLinkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MessageService messageService;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository, 
                              UserRepository userRepository,
                              JwtTokenProvider jwtTokenProvider,
                              MessageService messageService) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.messageService = messageService;
    }

    public List<ConversationResponse> getUserConversations(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        List<Conversation> conversations = conversationRepository.findByUser(user);
        return conversations.stream()
                .map(ConversationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ConversationResponse> searchConversations(String email, String keyword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        List<Conversation> conversations = conversationRepository.searchByKeyword(user, keyword);
        return conversations.stream()
                .map(ConversationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ConversationResponse createConversation(String email, ConversationCreateRequest request) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        Conversation conversation = new Conversation(request.getTitle(), user);
        conversation = conversationRepository.save(conversation);
        
        return ConversationResponse.fromEntity(conversation);
    }

    public ShareLinkResponse generateShareLink(String email, Long conversationId) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Vérifier que l'utilisateur est propriétaire de la conversation
        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to share this conversation");
        }
        
        // Générer un lien de partage (ici simplement l'ID, mais vous pourriez implémenter une logique plus complexe)
        String shareLink = "http://localhost:5173/shared/" + conversationId;
        
        return new ShareLinkResponse(shareLink);
    }

    public ConversationResponse getConversationWithMessages(String userEmail, Long conversationId) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        
        ConversationResponse conversation = getUserConversations(userEmail)
                .stream()
                .filter(c -> c.getId().equals(conversationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        // Récupérer les messages pour cette conversation
        conversation.setMessages(messageService.getMessagesForConversation(conversationId));
        
        return conversation;
    }
} 