package com.example.mygpt.infrastructure.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GeminiAIAdapterTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GeminiAIAdapterImpl geminiAIAdapter;

    @Test
    void shouldSendMessageAndReturnResponse() {
        String message = "Hello";
        String expectedResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Hi there!\"}]}}]}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));

        // Mock ObjectMapper behavior
        try {
            when(objectMapper.readTree(expectedResponse)).thenReturn(mock(com.fasterxml.jackson.databind.JsonNode.class));
            when(objectMapper.readTree(expectedResponse)
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText()).thenReturn("Hi there!");
        } catch (Exception e) {
            fail("Failed to mock ObjectMapper");
        }

        String result = geminiAIAdapter.sendMessage(message);

        assertEquals("Hi there!", result);
        verify(webClient, times(1)).post();
    }
}