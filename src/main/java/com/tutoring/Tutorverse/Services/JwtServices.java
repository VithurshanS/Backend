package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtServices {
    // Strong secret key for HS512 - must be at least 64 bytes (512 bits)
    @org.springframework.beans.factory.annotation.Value("${app.jwt.secret}")
    private String jwtSecret;
    private final long jwtExpirationMs = 86400000; // 1 day
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(User user){
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",user.getId());
        claims.put("roles",user.getRole().getId()); // Use role name instead of enum
        return Jwts.builder().setClaims(claims).setSubject(user.getEmail()).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)).signWith(getSigningKey(), SignatureAlgorithm.HS512).compact();

    }

    public boolean validateJwtToken(String authToken){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT validation error: " + e.getMessage());
        }
        return false;
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public UUID getUserIdFromJwtToken(String token) {
        Object val = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId");
        if (val == null) {
            throw new RuntimeException("userId claim missing");
        }
        return UUID.fromString(val.toString());
    }

    public Long getRoleIdFromJwtToken(String token) {
        Object val = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles");
        if (val == null) {
            throw new RuntimeException("roles claim missing");
        }
        return Long.valueOf(val.toString());
    }
}