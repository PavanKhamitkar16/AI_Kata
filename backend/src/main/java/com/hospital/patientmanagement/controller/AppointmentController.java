package com.hospital.patientmanagement.controller;

import com.hospital.patientmanagement.dto.ApiResponse;
import com.hospital.patientmanagement.dto.AppointmentRequest;
import com.hospital.patientmanagement.dto.AppointmentResponse;
import com.hospital.patientmanagement.dto.RescheduleRequest;
import com.hospital.patientmanagement.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for appointment management.
 *
 * <pre>
 * POST  /api/appointments                          — ADMIN | DOCTOR | STAFF
 * GET   /api/appointments/{id}                    — ADMIN | DOCTOR | STAFF
 * GET   /api/appointments/patient/{patientId}     — ADMIN | DOCTOR | STAFF
 * GET   /api/appointments/doctor/{doctorId}       — ADMIN | DOCTOR | STAFF
 * PUT   /api/appointments/{id}/confirm            — ADMIN | DOCTOR | STAFF
 * PUT   /api/appointments/{id}/cancel             — ADMIN | DOCTOR | STAFF
 * PUT   /api/appointments/{id}/reschedule         — ADMIN | DOCTOR | STAFF
 * PUT   /api/appointments/{id}/complete           — ADMIN | DOCTOR | STAFF
 * PUT   /api/appointments/{id}/no-show            — ADMIN | DOCTOR | STAFF
 * </pre>
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Returns all appointments in the system, newest first.
     * Intended for ADMIN and STAFF users who manage the full schedule.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAllAppointments() {
        List<AppointmentResponse> list = appointmentService.getAllAppointments();
        return ResponseEntity.ok(ApiResponse.success("All appointments retrieved", list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Appointment retrieved", appointmentService.getAppointment(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getPatientAppointments(
            @PathVariable UUID patientId) {
        List<AppointmentResponse> list = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient appointments retrieved", list));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDoctorAppointments(
            @PathVariable UUID doctorId) {
        List<AppointmentResponse> list = appointmentService.getDoctorAppointments(doctorId);
        return ResponseEntity.ok(ApiResponse.success("Doctor appointments retrieved", list));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Appointment confirmed", appointmentService.confirmAppointment(id)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Appointment cancelled", appointmentService.cancelAppointment(id)));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Appointment rescheduled",
                        appointmentService.rescheduleAppointment(id, request)));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Appointment completed", appointmentService.completeAppointment(id)));
    }

    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> markNoShow(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Appointment marked as no-show", appointmentService.markNoShow(id)));
    }
}
