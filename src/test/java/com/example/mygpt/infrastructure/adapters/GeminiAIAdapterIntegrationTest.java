package com.example.mygpt.infrastructure.adapters;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
public class GeminiAIAdapterIntegrationTest {

    @Autowired
    private GeminiAIAdapter geminiAIAdapter;

    @Test
    void shouldCallGeminiAPIAndReturnResponse() {
        String response = geminiAIAdapter.sendMessage("Hello, how are you?");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
}