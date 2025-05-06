package com.example.mygpt.domains.user.repositories;

import com.example.mygpt.domains.user.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = new User("john.doe@example.com", "password123");
        userRepository.save(user);

        User found = userRepository.findByEmail("john.doe@example.com");
        assertNotNull(found);
        assertEquals("john.doe@example.com", found.getEmail());
    }
}