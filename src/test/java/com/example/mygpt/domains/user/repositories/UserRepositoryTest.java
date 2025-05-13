package com.example.mygpt.domains.user.repositories;

import com.example.mygpt.domains.user.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    // Ce test vérifie que l'utilisateur peut être sauvegardé dans la base de données
    // et retrouvé par son email, en s'assurant que l'email correspond bien.
    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = new User("john.doe@example.com", passwordEncoder.encode("password123"));
        userRepository.save(user);

        User found = userRepository.findByEmail("john.doe@example.com");
        assertNotNull(found);
        assertEquals("john.doe@example.com", found.getEmail());
    }
}