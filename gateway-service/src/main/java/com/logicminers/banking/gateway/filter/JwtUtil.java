package com.logicminers.banking.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.jackson.io.JacksonDeserializer; // 🛑 THE NEW TOOL
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public void validateToken(final String token) {
        Jwts.parserBuilder()
                .deserializeJsonWith(new JacksonDeserializer<>()) // 🛑 THE FIX
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);
    }

    public String extractUsername(final String token) {
        return Jwts.parserBuilder()
                .deserializeJsonWith(new JacksonDeserializer<>()) // 🛑 THE FIX
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}