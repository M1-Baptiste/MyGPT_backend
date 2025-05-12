package com.example.mygpt.domains.conversation.repositories;

import com.example.mygpt.domains.conversation.entities.Conversation;
import com.example.mygpt.domains.conversation.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Trouve tous les messages d'une conversation
     */
    List<Message> findByConversationOrderByCreatedAt(Conversation conversation);
    
    /**
     * Recherche des messages par mot-clé dans le contenu
     */
    @Query("SELECT m FROM Message m WHERE m.conversation = ?1 AND LOWER(m.content) LIKE LOWER(CONCAT('%', ?2, '%'))")
    List<Message> searchByKeyword(Conversation conversation, String keyword);
    
    /**
     * Supprime tous les messages d'une conversation
     */
    void deleteByConversation(Conversation conversation);
} 