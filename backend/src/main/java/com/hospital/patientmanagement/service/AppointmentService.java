package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.AppointmentRequest;
import com.hospital.patientmanagement.dto.AppointmentResponse;
import com.hospital.patientmanagement.dto.RescheduleRequest;

import java.util.List;
import java.util.UUID;

/**
 * Business operations for appointment management.
 */
public interface AppointmentService {

    /**
     * Creates a new appointment with status REQUESTED.
     *
     * @throws IllegalArgumentException if the slot is already taken, the doctor is on leave,
     *                                  the date is not a working day, or the time is outside working hours
     */
    AppointmentResponse createAppointment(AppointmentRequest request);

    AppointmentResponse getAppointment(UUID appointmentId);

    /** Returns every appointment in the system, newest first. Intended for ADMIN / STAFF views. */
    List<AppointmentResponse> getAllAppointments();

    List<AppointmentResponse> getPatientAppointments(UUID patientId);

    List<AppointmentResponse> getDoctorAppointments(UUID doctorId);

    /** REQUESTED → CONFIRMED */
    AppointmentResponse confirmAppointment(UUID appointmentId);

    /** REQUESTED | CONFIRMED → CANCELLED */
    AppointmentResponse cancelAppointment(UUID appointmentId);

    /**
     * Moves appointment to a new date/time (must be free).
     * Status stays REQUESTED.
     */
    AppointmentResponse rescheduleAppointment(UUID appointmentId, RescheduleRequest request);

    /** CONFIRMED → COMPLETED */
    AppointmentResponse completeAppointment(UUID appointmentId);

    /** CONFIRMED → NO_SHOW */
    AppointmentResponse markNoShow(UUID appointmentId);
}
