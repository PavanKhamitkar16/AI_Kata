package com.hospital.patientmanagement.controller;

import com.hospital.patientmanagement.dto.ApiResponse;
import com.hospital.patientmanagement.dto.DoctorScheduleRequest;
import com.hospital.patientmanagement.dto.DoctorScheduleResponse;
import com.hospital.patientmanagement.dto.TimeSlotResponse;
import com.hospital.patientmanagement.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for doctor working-schedule management.
 *
 * <pre>
 * POST  /api/doctors/{doctorId}/schedule                      — ADMIN only
 * GET   /api/doctors/{doctorId}/schedule                      — ADMIN | DOCTOR | STAFF
 * GET   /api/doctors/{doctorId}/available-slots?date=YYYY-MM-DD — ADMIN | DOCTOR | STAFF
 * </pre>
 */
@RestController
@RequestMapping("/api/doctors/{doctorId}")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorScheduleResponse>> saveSchedule(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorScheduleRequest request) {
        DoctorScheduleResponse response = scheduleService.saveSchedule(doctorId, request);
        return ResponseEntity.ok(ApiResponse.success("Schedule saved", response));
    }

    @GetMapping("/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<DoctorScheduleResponse>> getSchedule(
            @PathVariable UUID doctorId) {
        return ResponseEntity.ok(
                ApiResponse.success("Schedule retrieved", scheduleService.getSchedule(doctorId)));
    }

    @GetMapping("/available-slots")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TimeSlotResponse> slots = scheduleService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(ApiResponse.success("Available slots retrieved", slots));
    }
}
