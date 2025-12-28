package com.molla.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtProvider {

    // Secret key used to sign JWT (same key used in JwtValidator)
    private static final SecretKey key =
            Keys.hmacShaKeyFor(JwtConstant.JWT_SECRET.getBytes());

    /**
     * Generate JWT token after successful login
     */
    public String generateToken(Authentication authentication) {

        // 1. Get logged-in user's roles
        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        String roles = populateAuthorities(authorities);

        // 2. Build JWT token
        return Jwts.builder()
                .setSubject(authentication.getName()) // email / username
                .claim("role", roles)                  // user roles
                .setIssuedAt(new Date())               // token creation time
                .setExpiration(
                        new Date(System.currentTimeMillis() + 86400000) // 1 day
                )
                .signWith(key)                         // sign token
                .compact();
    }

    /**
     * Convert authorities into comma-separated string
     */
    private String populateAuthorities(
            Collection<? extends GrantedAuthority> authorities
    ) {
        Set<String> roles = new HashSet<>();

        for (GrantedAuthority authority : authorities) {
            roles.add(authority.getAuthority());
        }

        return String.join(",", roles);
    }

    /**
     * Extract email (subject) from JWT token
     */
    public String getEmailFromToken(String token) {

        // Remove "Bearer " if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}