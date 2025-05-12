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

    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = new User("john.doe@example.com", passwordEncoder.encode("password123"));
        userRepository.save(user);

        User found = userRepository.findByEmail("john.doe@example.com");
        assertNotNull(found);
        assertEquals("john.doe@example.com", found.getEmail());
    }
}