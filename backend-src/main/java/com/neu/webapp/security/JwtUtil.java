package com.neu.webapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "parcel-locker-system-jwt-secret-key-2026";
    private static final long EXPIRE_SHORT = 2 * 60 * 60 * 1000L; // 2 hours
    private static final long EXPIRE_LONG = 7 * 24 * 60 * 60 * 1000L; // 7 days

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role, boolean rememberMe) {
        long expire = rememberMe ? EXPIRE_LONG : EXPIRE_SHORT;
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getKey())
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(validateToken(token).getSubject());
    }

    public String getUsernameFromToken(String token) {
        return validateToken(token).get("username", String.class);
    }

    public String getRoleFromToken(String token) {
        return validateToken(token).get("role", String.class);
    }
}
