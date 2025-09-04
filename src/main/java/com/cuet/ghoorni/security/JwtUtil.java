package com.cuet.ghoorni.security;

import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // Use environment variable for JWT secret key for security
    private final String SECRET_KEY = System.getenv("JWT_SECRET") != null ? System.getenv("JWT_SECRET")
            : "ghoorni_secure_jwt_key_for_application_must_be_at_least_32_chars_long";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 days
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            String subject = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody()
                    .getSubject();
            System.out.println("Extracted user ID from token: " + subject);
            return subject;
        } catch (Exception e) {
            System.err.println("Error extracting username from token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            System.err.println("Token expiration check failed: " + e.getMessage());
            return true; // Consider expired if we can't parse it
        }
    }
}