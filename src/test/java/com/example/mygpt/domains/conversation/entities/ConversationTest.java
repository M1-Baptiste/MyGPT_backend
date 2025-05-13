package com.example.mygpt.domains.conversation.entities;

import com.example.mygpt.domains.user.entities.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConversationTest {

    // Ce test vérifie que la création d'une conversation avec des données valides
    // fonctionne correctement, en s'assurant que le titre, l'utilisateur et la date
    // de création sont correctement définis.
    @Test
    void shouldCreateConversationWithValidData() {
        User user = new User("john.doe@example.com", "password123");
        Conversation conversation = new Conversation("Test Conversation", user);
        assertEquals("Test Conversation", conversation.getTitle());
        assertEquals(user, conversation.getUser());
        assertNotNull(conversation.getCreatedAt());
    }
}