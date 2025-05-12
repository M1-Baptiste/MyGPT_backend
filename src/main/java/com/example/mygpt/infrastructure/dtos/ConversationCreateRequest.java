package com.example.mygpt.infrastructure.dtos;

public class ConversationCreateRequest {
    private String title;

    public ConversationCreateRequest() {
    }

    public ConversationCreateRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
} 