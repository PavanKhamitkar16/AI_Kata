package com.hospital.patientmanagement.enums;

/**
 * Lifecycle states for an {@code Appointment}.
 *
 * <pre>
 * REQUESTED  → CONFIRMED → COMPLETED
 *                        → NO_SHOW
 *           → CANCELLED  (from REQUESTED or CONFIRMED)
 * </pre>
 */
public enum AppointmentStatus {
    /** Patient has requested; awaiting doctor/staff confirmation. */
    REQUESTED,
    /** Doctor or staff has confirmed the appointment. */
    CONFIRMED,
    /** Appointment was cancelled by patient, doctor, or staff. */
    CANCELLED,
    /** Appointment took place and is marked complete. */
    COMPLETED,
    /** Patient did not show up for a confirmed appointment. */
    NO_SHOW
}
