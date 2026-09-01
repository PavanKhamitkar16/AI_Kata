package com.hospital.patientmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response payload for a successful login.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType;
    private String username;
    private String email;
    private String role;
    private long   expiresIn; // milliseconds
}
