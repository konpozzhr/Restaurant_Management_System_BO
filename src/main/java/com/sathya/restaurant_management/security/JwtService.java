package com.sathya.restaurant_management.security;

import com.sathya.restaurant_management.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Creates and checks the signed tokens the browser sends in "Authorization: Bearer ...". */
@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final Duration lifetime;

    public JwtService(@Value("${app.jwt.secret:}") String secret,
                      @Value("${app.jwt.expiration-minutes:480}") long expirationMinutes) {
        byte[] keyBytes;
        if (secret == null || secret.isBlank()) {
            keyBytes = new byte[48];
            new SecureRandom().nextBytes(keyBytes);
            log.warn("JWT_SECRET is not set - using a random signing key. "
                    + "Everyone is signed out whenever the server restarts. Set JWT_SECRET (32+ characters) for real use.");
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET must be at least 32 characters long.");
            }
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.lifetime = Duration.ofMinutes(expirationMinutes);
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }

    public IssuedToken issue(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(lifetime);
        String token = Jwts.builder()
                .subject(user.getId())
                .claim("tv", user.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    /** Empty when the token is malformed, tampered with, or expired. */
    public Optional<Claims> parse(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
