package com.ims_web.inventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // ⚠️ should eventually move to application.properties
    private static final String SECRET =
            "super-secret-key-super-secret-key-123456";

    private static final long EXPIRATION = 1000 * 60 * 60; // 1 hour

    private final MacAlgorithm alg = Jwts.SIG.HS256;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // =========================
    // TOKEN GENERATION
    // =========================
    public String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))

                // 🔥 future-proofing: room for roles/permissions if needed later
                .claim("type", "access")

                .signWith(getKey(), alg)
                .compact();
    }

    // =========================
    // CORE PARSING (single source)
    // =========================
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =========================
    // EMAIL EXTRACTION
    // =========================
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // =========================
    // VALIDATION
    // =========================
    public boolean isValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }
}