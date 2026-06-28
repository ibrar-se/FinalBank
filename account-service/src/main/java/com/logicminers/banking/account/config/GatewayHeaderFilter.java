package com.logicminers.banking.account.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Look for the "sticky note" attached by the API Gateway
        String username = request.getHeader("X-Auth-Username");

        // 2. If the Gateway attached a verified username, and we haven't authenticated them yet...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            System.out.println("🔐 GatewayHeaderFilter: Trusted request received for user -> " + username);

            // 3. Create a digital ID card for Spring Security to use internally
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

            // 4. Lock this ID card into the Spring Security Vault
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 5. Pass the request along to your AccountController
        filterChain.doFilter(request, response);
    }
}