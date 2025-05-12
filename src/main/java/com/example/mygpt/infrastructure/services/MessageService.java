package com.example.mygpt.infrastructure.services;

import com.example.mygpt.domains.conversation.entities.Conversation;
import com.example.mygpt.domains.conversation.entities.Message;
import com.example.mygpt.domains.conversation.repositories.ConversationRepository;
import com.example.mygpt.domains.conversation.repositories.MessageRepository;
import com.example.mygpt.domains.user.entities.User;
import com.example.mygpt.domains.user.repositories.UserRepository;
import com.example.mygpt.infrastructure.adapters.GeminiAIAdapter;
import com.example.mygpt.infrastructure.dtos.MessageRequest;
import com.example.mygpt.infrastructure.dtos.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GeminiAIAdapter geminiAIAdapter;

    public MessageService(UserRepository userRepository, 
                         GeminiAIAdapter geminiAIAdapter,
                         ConversationRepository conversationRepository,
                         MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.geminiAIAdapter = geminiAIAdapter;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public List<MessageResponse> createMessage(String userEmail, Long conversationId, MessageRequest request) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        // Vérifier que l'utilisateur est propriétaire de la conversation
        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous n'avez pas la permission d'ajouter des messages à cette conversation");
        }
        
        // Créer et sauvegarder le message utilisateur
        Message userMessage = new Message(request.getContent(), true, conversation);
        userMessage = messageRepository.save(userMessage);
        
        // Générer et sauvegarder la réponse AI
        MessageResponse aiResponse = geminiAIAdapter.generateResponse(request.getContent());
        Message aiMessage = new Message(aiResponse.getContent(), false, conversation);
        aiMessage = messageRepository.save(aiMessage);
        
        // Retourner à la fois le message utilisateur et la réponse AI
        return Arrays.asList(convertToDto(userMessage), convertToDto(aiMessage));
    }
    
    @Transactional
    public MessageResponse createAIMessage(Long conversationId, MessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        MessageResponse aiResponse = geminiAIAdapter.generateResponse(request.getContent());
        
        Message aiMessage = new Message(aiResponse.getContent(), false, conversation);
        aiMessage = messageRepository.save(aiMessage);
        
        return convertToDto(aiMessage);
    }
    
    @Transactional
    public List<MessageResponse> updateMessage(String userEmail, Long conversationId, Long messageId, MessageRequest request) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        // Vérifier que l'utilisateur est propriétaire de la conversation
        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous n'avez pas la permission de modifier des messages dans cette conversation");
        }
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));
        
        // Vérifier que le message appartient à la conversation
        if (!message.getConversation().getId().equals(conversationId)) {
            throw new RuntimeException("Le message n'appartient pas à cette conversation");
        }
        
        // Vérifier que c'est un message utilisateur
        if (!message.isUserMessage()) {
            throw new RuntimeException("Seuls les messages utilisateur peuvent être modifiés");
        }
        
        // Mettre à jour le message
        message.setContent(request.getContent());
        message = messageRepository.save(message);
        
        Message aiMessage = null;
        
        // Trouver le message AI suivant et le supprimer si nécessaire
        List<Message> messages = messageRepository.findByConversationOrderByCreatedAt(conversation);
        int index = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(messageId)) {
                index = i;
                break;
            }
        }
        
        if (index != -1 && index + 1 < messages.size() && !messages.get(index + 1).isUserMessage()) {
            // Supprimer l'ancienne réponse AI
            messageRepository.delete(messages.get(index + 1));
            
            // Générer une nouvelle réponse AI et la sauvegarder
            MessageResponse aiResponse = geminiAIAdapter.generateResponse(request.getContent());
            aiMessage = new Message(aiResponse.getContent(), false, conversation);
            aiMessage = messageRepository.save(aiMessage);
        } else {
            // Si aucun message AI n'a été trouvé après le message utilisateur, en créer un nouveau
            MessageResponse aiResponse = geminiAIAdapter.generateResponse(request.getContent());
            aiMessage = new Message(aiResponse.getContent(), false, conversation);
            aiMessage = messageRepository.save(aiMessage);
        }
        
        // Retourner le message utilisateur mis à jour et la nouvelle réponse AI
        return Arrays.asList(convertToDto(message), convertToDto(aiMessage));
    }
    
    public List<MessageResponse> getMessagesForConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        List<Message> messages = messageRepository.findByConversationOrderByCreatedAt(conversation);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<MessageResponse> searchMessages(String userEmail, Long conversationId, String keyword) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        // Vérifier que l'utilisateur est propriétaire de la conversation
        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous n'avez pas la permission de rechercher des messages dans cette conversation");
        }
        
        List<Message> messages = messageRepository.searchByKeyword(conversation, keyword);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteMessagesForConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        messageRepository.deleteByConversation(conversation);
    }
    
    private MessageResponse convertToDto(Message message) {
        MessageResponse dto = new MessageResponse();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setUserMessage(message.isUserMessage());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
} 