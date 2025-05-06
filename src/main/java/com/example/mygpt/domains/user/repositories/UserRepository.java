package com.example.mygpt.domains.user.repositories;


import com.example.mygpt.domains.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}