package com.example.mygpt.infrastructure.config;

import com.example.mygpt.domains.user.entities.User;
import com.example.mygpt.domains.user.repositories.UserRepository;
import com.example.mygpt.infrastructure.adapters.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            logger.info("JWT Token reçu: {}", jwt);

            if (jwt != null) {
                try {
                    if (jwtTokenProvider.validateToken(jwt)) {
                        String userEmail = jwtTokenProvider.getEmailFromToken(jwt);
                        logger.info("Email extrait du token: {}", userEmail);
                        
                        User user = userRepository.findByEmail(userEmail);
                        if (user != null) {
                            // Créer une authentification Spring Security
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userEmail, null, new ArrayList<>());
                            
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            // Définir l'authentification dans le contexte de sécurité
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.info("Utilisateur {} authentifié avec succès", userEmail);
                        } else {
                            logger.warn("Utilisateur avec l'email {} non trouvé dans la base de données", userEmail);
                        }
                    } else {
                        logger.warn("Token JWT invalide");
                    }
                } catch (Exception e) {
                    logger.error("Impossible de valider le token JWT: {}", e.getMessage(), e);
                }
            } else {
                logger.warn("Aucun token JWT trouvé dans la requête");
            }
        } catch (Exception e) {
            logger.error("Erreur dans le filtre JWT: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 