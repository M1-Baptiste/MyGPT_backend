package com.example.mygpt.services;

import com.example.mygpt.domains.user.entities.User;
import com.example.mygpt.domains.user.repositories.UserRepository;
import com.example.mygpt.infrastructure.adapters.JwtTokenProvider;
import com.example.mygpt.infrastructure.dtos.LoginRequest;
import com.example.mygpt.infrastructure.dtos.LoginResponse;
import com.example.mygpt.infrastructure.dtos.RegisterRequest;
import com.example.mygpt.infrastructure.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");

        user = new User("john.doe@example.com", "encodedPassword");
        user.setId(1L);

        // Explicitly initialize AuthService with mocks
        authService = new AuthService(userRepository, passwordEncoder);
        authService.jwtTokenProvider = jwtTokenProvider;
    }

    // Ce test vérifie que l'enregistrement d'un nouvel utilisateur fonctionne correctement
    // lorsque l'email n'existe pas encore, et que le mot de passe est correctement encodé.
    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        User savedUser = new User(registerRequest.getEmail(), "encodedPassword");
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(registerRequest);

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    // Ce test vérifie qu'une exception est levée lorsque l'email existe déjà
    // lors de la tentative d'enregistrement.
    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).findByEmail(registerRequest.getEmail());
        verifyNoMoreInteractions(passwordEncoder, userRepository);
    }

    // Ce test vérifie que la connexion fonctionne correctement avec des identifiants valides
    // et retourne un token JWT et l'email de l'utilisateur.
    @Test
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getId(), user.getEmail())).thenReturn("jwtToken");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(user.getEmail(), response.getEmail());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(jwtTokenProvider).generateToken(user.getId(), user.getEmail());
    }

    // Ce test vérifie qu'une exception BadCredentialsException est levée
    // lorsque l'email est invalide (n'existe pas).
    @Test
    void shouldThrowBadCredentialsExceptionForInvalidEmail() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(null);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verifyNoInteractions(passwordEncoder, jwtTokenProvider);
    }

    // Ce test vérifie qu'une exception BadCredentialsException est levée
    // lorsque le mot de passe est incorrect.
    @Test
    void shouldThrowBadCredentialsExceptionForInvalidPassword() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verifyNoInteractions(jwtTokenProvider);
    }
}