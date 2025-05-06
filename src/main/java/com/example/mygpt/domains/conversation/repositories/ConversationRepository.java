package com.example.mygpt.domains.conversation.repositories;

import com.example.mygpt.domains.conversation.entities.Conversation;
import com.example.mygpt.domains.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUser(User user);
}