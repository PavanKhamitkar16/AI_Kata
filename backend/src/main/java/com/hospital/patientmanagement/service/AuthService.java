package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.LoginRequest;
import com.hospital.patientmanagement.dto.LoginResponse;

/**
 * Contract for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticates the user and returns a signed JWT.
     *
     * @param request username + password
     * @return {@link LoginResponse} containing the JWT and user metadata
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         if credentials are invalid
     */
    LoginResponse login(LoginRequest request);

    /**
     * Invalidates the supplied JWT by adding it to the blacklist.
     *
     * @param bearerToken the raw {@code Authorization: Bearer <token>} header value
     */
    void logout(String bearerToken);
}
