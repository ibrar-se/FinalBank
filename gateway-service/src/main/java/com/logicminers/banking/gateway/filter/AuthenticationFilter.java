package com.logicminers.banking.gateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    // 🟢 THE VAULT CONNECTION: Reactive Redis template for non-blocking lookups
    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        System.out.println("\n================ GATEWAY REQUEST ================");
        System.out.println("URI: " + exchange.getRequest().getURI());
        System.out.println("Path: " + path);
        System.out.println("Method: " + exchange.getRequest().getMethod());

        boolean secured = validator.isSecured.test(exchange.getRequest());
        System.out.println("Secured Route? " + secured);

        // 🔓 Skip auth for public endpoints
        if (!secured) {
            System.out.println("PUBLIC ROUTE → skipping auth filter");
            return chain.filter(exchange);
        }

        System.out.println("🔐 SECURED ROUTE → checking JWT");

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // 🟢 THE BLACKLIST CHECKPOINT: Query Redis asynchronously
        return redisTemplate.hasKey(token)
                .flatMap(isBlacklisted -> {

                    // If Redis contains the token key, the user has logged out!
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        System.out.println("❌ SECURITY ALERT: Attempted access with blacklisted JWT token!");
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    // If NOT blacklisted, continue with normal cryptographic validation
                    try {
                        // 1. Verify the signature
                        jwtUtil.validateToken(token);
                        System.out.println("✅ JWT VALID");

                        // 2. Extract the username
                        String username = jwtUtil.extractUsername(token);
                        System.out.println("👤 Identified User: " + username);

                        // 3. The Sanitizer and Mutation
                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(exchange.getRequest().mutate()
                                        .headers(headers -> headers.remove("X-Auth-Username"))
                                        .header("X-Auth-Username", username)
                                        .build())
                                .build();

                        System.out.println("➡️ Forwarding secured request to internal network...");
                        return chain.filter(mutatedExchange);

                    } catch (Exception e) {
                        System.out.println("❌ JWT INVALID: " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                });
    }
}