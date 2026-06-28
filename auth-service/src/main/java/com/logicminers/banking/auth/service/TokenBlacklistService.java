package com.logicminers.banking.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    // Spring Boot automatically injects this tool to talk to Redis
    private final StringRedisTemplate redisTemplate;


    public void blacklistToken(String token, long timeToLiveInMillis) {
        // We use the token itself as the 'Key', and just put "blacklisted" as the 'Value'
        // We tell Redis to automatically delete this entry when the token naturally expires to save RAM!
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofMillis(timeToLiveInMillis));

        System.out.println("🔒 REDIS: Token successfully blacklisted!");
    }
}