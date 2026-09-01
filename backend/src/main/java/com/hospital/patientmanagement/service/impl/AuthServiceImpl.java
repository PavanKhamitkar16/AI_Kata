package com.hospital.patientmanagement.service.impl;

import com.hospital.patientmanagement.dto.LoginRequest;
import com.hospital.patientmanagement.dto.LoginResponse;
import com.hospital.patientmanagement.security.CustomUserDetails;
import com.hospital.patientmanagement.security.JwtTokenProvider;
import com.hospital.patientmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Default {@link AuthService} implementation.
 *
 * <p>Delegates credential verification to Spring Security's
 * {@link AuthenticationManager}, which invokes the
 * {@link com.hospital.patientmanagement.security.CustomUserDetailsService}
 * and BCrypt comparison configured in
 * {@link com.hospital.patientmanagement.config.SecurityConfig}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      jwtTokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Throws BadCredentialsException / LockedException on failure —
        // GlobalExceptionHandler converts those to HTTP 401.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);

        log.info("User '{}' logged in successfully.", principal.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(principal.getUsername())
                .email(principal.getUser().getEmail())
                .role(principal.getUser().getRole().name())
                .expiresIn(jwtExpirationMs)
                .build();
    }

    @Override
    public void logout(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            jwtTokenProvider.blacklistToken(token);
            log.debug("Token blacklisted on logout.");
        }
    }
}
