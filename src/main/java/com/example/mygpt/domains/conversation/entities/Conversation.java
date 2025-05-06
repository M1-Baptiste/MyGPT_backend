package com.example.mygpt.domains.conversation.entities;

import com.example.mygpt.domains.user.entities.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    private User user;

    private LocalDateTime createdAt;

    public Conversation() {}

    public Conversation(String title, User user) {
        this.title = title;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}