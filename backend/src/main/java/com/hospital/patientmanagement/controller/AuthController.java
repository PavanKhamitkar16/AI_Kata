package com.hospital.patientmanagement.controller;

import com.hospital.patientmanagement.dto.ApiResponse;
import com.hospital.patientmanagement.dto.LoginRequest;
import com.hospital.patientmanagement.dto.LoginResponse;
import com.hospital.patientmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 *
 * <pre>
 * POST /api/auth/login   — public, returns JWT
 * POST /api/auth/logout  — requires valid JWT, blacklists it
 * </pre>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user and returns a signed JWT.
     *
     * <p>Returns HTTP 200 on success, 401 on bad credentials (handled by
     * {@link com.hospital.patientmanagement.exception.GlobalExceptionHandler}).
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", loginResponse));
    }

    /**
     * Logs the user out by blacklisting the current JWT.
     *
     * <p>The client must also discard its local copy of the token.
     * Returns HTTP 200 whether or not the token was already blacklisted.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        authService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }
}
