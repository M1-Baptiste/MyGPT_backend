package com.example.mygpt.domains.user.entities;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private final PasswordEncoder passwordEncoder;

    public UserTest(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Ce test vérifie que la création d'un utilisateur avec des données valides
    // fonctionne correctement, en s'assurant que l'email et le mot de passe
    // sont correctement définis.
    @Test
    void shouldCreateUserWithValidData() {
        User user = new User("john.doe@example.com", passwordEncoder.encode("password123"));
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }

    // Ce test vérifie que la création d'un utilisateur avec un email invalide
    // lève une exception IllegalArgumentException avec le message approprié.
    @Test
    void shouldThrowExceptionForInvalidEmail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("invalid-email", passwordEncoder.encode("password123"));
        });
        assertEquals("Invalid email format", exception.getMessage());
    }
}