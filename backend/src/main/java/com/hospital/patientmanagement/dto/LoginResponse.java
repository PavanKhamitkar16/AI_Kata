package com.hospital.patientmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response payload for a successful login.
 *
 * <p>{@code doctorId} is populated only when the authenticated user has the DOCTOR role
 * and a matching Doctor profile (matched by email) exists in the system. It is {@code null}
 * for ADMIN and STAFF users.
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

    /** UUID of the Doctor profile linked to this user, or {@code null} if not a doctor. */
    private UUID doctorId;
}
