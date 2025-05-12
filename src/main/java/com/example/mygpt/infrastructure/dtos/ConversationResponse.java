package com.example.mygpt.infrastructure.dtos;

import com.example.mygpt.domains.conversation.entities.Conversation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConversationResponse {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private List<MessageResponse> messages = new ArrayList<>();

    public ConversationResponse() {
    }

    public ConversationResponse(Long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }
    
    public static ConversationResponse fromEntity(Conversation conversation) {
        return new ConversationResponse(
            conversation.getId(),
            conversation.getTitle(),
            conversation.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<MessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageResponse> messages) {
        this.messages = messages;
    }
} 