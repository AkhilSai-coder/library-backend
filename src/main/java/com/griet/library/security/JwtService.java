package com.griet.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // ✅ FIXED: No hyphens, min 32 chars for HS256, using Keys.hmacShaKeyFor()
    private static final String SECRET_KEY = "librarySuperSecretKey9876543210AB";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

// ==============================
// GENERATE TOKEN
// ==============================

    public String generateToken(String collegeId, String role) {

        return Jwts.builder()
                .setSubject(collegeId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)
                ) // 24 hours
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

// ==============================
// EXTRACT USERNAME (COLLEGE ID)
// ==============================

    public String extractUsername(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

}