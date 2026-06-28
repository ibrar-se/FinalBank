package com.logicminers.banking.auth.controller;

import com.logicminers.banking.auth.dto.AuthResponse;
import com.logicminers.banking.auth.dto.LoginRequest;
import com.logicminers.banking.auth.dto.RegisterRequest;
import com.logicminers.banking.auth.dto.TokenRefreshRequest;
import com.logicminers.banking.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// This matches the exact path we left completely open in your SecurityConfig!
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Pass the validated payload straight to the brain
            String response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 🛑 THE FIX: Catch the duplicate email SQL error cleanly!
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registration Failed: Username or Email already exists!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration Failed: " + e.getMessage());
        }
    }

    // 🛑 UPDATED: Use ResponseEntity<?> so we can return either the AuthResponse OR a String error
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Pass the credentials to the brain to be checked
            AuthResponse response = authService.login(request);

            // Return a 200 OK status with the fresh JWT token and Refresh token in the body
            return ResponseEntity.ok(response);

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // 🛑 THE FIX: Catch the bad password and return a 401 instead of a 403!
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Login Failed: Invalid username or password.");

        } catch (Exception e) {
            // Catch anything else (like an account being locked or disabled)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Login Failed: " + e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        // Pass the token from the URL straight to the brain
        String response = authService.verifyEmail(token);

        // Return a 200 OK status
        return ResponseEntity.ok(response);
    }

    // --- NEW: The Silent Refresh Endpoint ---
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        // Pass the old refresh token to the brain to get a new access token
        AuthResponse response = authService.refreshToken(request);

        // Return a 200 OK status with the new access token
        return ResponseEntity.ok(response);
    }
    // --- NEW: The Logout Endpoint ---
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody TokenRefreshRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // Pass both the body (refresh token) and the header (access token) to the brain
        String response = authService.logout(request, authHeader);
        return ResponseEntity.ok(response);
    }
    // 🛑 TEMPORARY TEST: Proves the Gateway injected the header!
    @GetMapping("/test-gateway")
    public ResponseEntity<String> testGateway(@RequestHeader(value = "X-Auth-Username", defaultValue = "HEADER MISSING") String username) {
        if ("HEADER MISSING".equals(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("FAIL: The Gateway let you through, but forgot to inject the X-Auth-Username header!");
        }
        return ResponseEntity.ok("SUCCESS! The Gateway verified the JWT and injected this username: " + username);
    }

}