package com.example.mygpt.domains.user.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        User user = new User("john.doe@example.com", "password123");
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("invalid-email", "password123");
        });
        assertEquals("Invalid email format", exception.getMessage());
    }
}