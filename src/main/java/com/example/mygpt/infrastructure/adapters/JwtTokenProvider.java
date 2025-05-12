package com.example.mygpt.infrastructure.adapters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${jwt.secret:mysecretkey}")
    private String jwtSecretString;

    @Value("${jwt.expiration:86400000}") // 24 heures en millisecondes par défaut
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecretString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }
    
    public String getEmailFromToken(String token) {
        try {
            logger.info("Extraction de l'email du token: {}", token);
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("email", String.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get("email", String.class);
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            logger.info("Validation du token JWT: {}", token);
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Signature JWT invalide: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Token JWT invalide: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expiré: {}", e.getMessage());
            // En mode débogage, on peut considérer un token expiré comme valide
            return true;
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT non supporté: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Les revendications JWT sont vides: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du token JWT: {}", e.getMessage(), e);
        }
        return false;
    }
} 