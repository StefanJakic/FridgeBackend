package com.hylastix.fridge.config;

import com.hylastix.fridge.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    // Generate a key that is at least 32 bytes (256 bits)
    private SecretKey getSigningKey() {
        if (secret != null && !secret.isEmpty()) {
            if (secret.length() >= 32) {  // Minimum 32 bytes (256 bits)
                return Keys.hmacShaKeyFor(secret.getBytes());
            } else {
                throw new IllegalArgumentException("Secret key is too short, it must be at least 32 bytes.");
            }
        } else {
            // If secret is not set, generate a new 256-bit key
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("userId", user.getId());

        return Jwts.builder().claims(claims).subject(user.getEmail()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSigningKey()) // Use secure key for validation
                .build().parseClaimsJws(token).getBody();
    }
}
