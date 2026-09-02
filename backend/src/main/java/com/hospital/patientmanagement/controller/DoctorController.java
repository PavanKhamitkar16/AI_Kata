package com.hospital.patientmanagement.controller;

import com.hospital.patientmanagement.dto.ApiResponse;
import com.hospital.patientmanagement.dto.DoctorRequest;
import com.hospital.patientmanagement.dto.DoctorResponse;
import com.hospital.patientmanagement.enums.AvailabilityStatus;
import com.hospital.patientmanagement.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST endpoints for doctor profile management.
 *
 * <pre>
 * POST   /api/doctors               — ADMIN only
 * PUT    /api/doctors/{id}          — ADMIN only
 * GET    /api/doctors/{id}          — ADMIN | DOCTOR | STAFF
 * GET    /api/doctors               — ADMIN | DOCTOR | STAFF  (paginated + filtered)
 * PUT    /api/doctors/{id}/activate   — ADMIN only
 * PUT    /api/doctors/{id}/deactivate — ADMIN only
 * </pre>
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(
            @Valid @RequestBody DoctorRequest request) {
        DoctorResponse response = doctorService.createDoctor(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @PathVariable UUID id,
            @Valid @RequestBody DoctorRequest request) {
        DoctorResponse response = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Doctor retrieved", doctorService.getDoctorById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<DoctorResponse>>> getDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) AvailabilityStatus status,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<DoctorResponse> page =
                doctorService.getDoctors(specialization, department, status, activeOnly, pageable);
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved", page));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> activateDoctor(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Doctor activated", doctorService.activateDoctor(id)));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> deactivateDoctor(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Doctor deactivated", doctorService.deactivateDoctor(id)));
    }
}
