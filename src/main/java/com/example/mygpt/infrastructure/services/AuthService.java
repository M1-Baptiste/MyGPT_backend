package com.example.mygpt.infrastructure.services;

import com.example.mygpt.domains.user.entities.User;
import com.example.mygpt.domains.user.repositories.UserRepository;
import com.example.mygpt.infrastructure.adapters.JwtTokenProvider;
import com.example.mygpt.infrastructure.dtos.LoginRequest;
import com.example.mygpt.infrastructure.dtos.LoginResponse;
import com.example.mygpt.infrastructure.dtos.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }

        // Encoder le mot de passe et créer un nouvel utilisateur
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getEmail(), encodedPassword);
        
        // Sauvegarder l'utilisateur
        userRepository.save(user);
    }
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, user.getEmail());
    }
} 