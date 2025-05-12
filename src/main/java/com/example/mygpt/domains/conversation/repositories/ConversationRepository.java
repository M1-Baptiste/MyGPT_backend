package com.example.mygpt.domains.conversation.repositories;

import com.example.mygpt.domains.conversation.entities.Conversation;
import com.example.mygpt.domains.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    List<Conversation> findByUser(User user);
    
    @Query("SELECT c FROM Conversation c WHERE c.user = :user AND LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Conversation> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);
}