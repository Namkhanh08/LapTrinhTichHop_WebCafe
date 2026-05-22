package com.example.Java_Backend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class JwtService {
    private static final String SECRET = "SUPERKEY_VU_NAM_KHANH_KEY_08112005";

    public String extractUserId(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
