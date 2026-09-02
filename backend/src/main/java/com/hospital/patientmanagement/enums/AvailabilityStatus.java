package com.hospital.patientmanagement.enums;

/**
 * Availability status for a doctor.
 * Stored as VARCHAR in the {@code doctors} table.
 */
public enum AvailabilityStatus {
    AVAILABLE,
    UNAVAILABLE,
    ON_LEAVE
}
