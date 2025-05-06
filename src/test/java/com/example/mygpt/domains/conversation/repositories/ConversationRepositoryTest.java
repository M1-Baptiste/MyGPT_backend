package com.example.mygpt.domains.conversation.repositories;

import com.example.mygpt.domains.conversation.entities.Conversation;
import com.example.mygpt.domains.user.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private com.example.mygpt.domains.user.repositories.UserRepository userRepository;

    @Test
    void shouldSaveAndFindConversationByUser() {
        User user = new User("john.doe@example.com", "password123");
        userRepository.save(user);

        Conversation conversation = new Conversation("Test Conversation", user);
        conversationRepository.save(conversation);

        var conversations = conversationRepository.findByUser(user);
        assertFalse(conversations.isEmpty());
        assertEquals("Test Conversation", conversations.get(0).getTitle());
    }
}