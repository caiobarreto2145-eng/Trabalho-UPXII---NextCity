package com.example.ecopontosapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Chave usada para "assinar" os tokens, provando que foram gerados por nós.
    // Em produção isso viria de uma variável de ambiente, não do código-fonte.
    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "ecopontos-chave-secreta-super-segura-com-pelo-menos-32-bytes".getBytes()
    );

    private final long expirationMs = 1000 * 60 * 60 * 24; // 24 horas

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}