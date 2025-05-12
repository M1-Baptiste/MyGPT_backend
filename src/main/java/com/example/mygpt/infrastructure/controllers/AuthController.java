package com.example.mygpt.infrastructure.controllers;

import com.example.mygpt.infrastructure.dtos.LoginRequest;
import com.example.mygpt.infrastructure.dtos.LoginResponse;
import com.example.mygpt.infrastructure.dtos.RegisterRequest;
import com.example.mygpt.infrastructure.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok().body(
                    new Object() {
                        public final String message = "User registered successfully";
                    }
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Object() {
                        public final String message = e.getMessage();
                    }
            );
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(
                    new Object() {
                        public final String message = e.getMessage();
                    }
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Object() {
                        public final String message = "An error occurred during login";
                    }
            );
        }
    }
} 