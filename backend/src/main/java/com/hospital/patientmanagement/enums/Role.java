package com.hospital.patientmanagement.enums;

/**
 * Application roles.
 *
 * <p>Stored as a {@code VARCHAR} in the {@code users} table.
 * Spring Security prefix convention: ROLE_ is added in
 * {@link com.hospital.patientmanagement.security.CustomUserDetails#getAuthorities()}.
 */
public enum Role {
    ADMIN,
    DOCTOR,
    STAFF
}
