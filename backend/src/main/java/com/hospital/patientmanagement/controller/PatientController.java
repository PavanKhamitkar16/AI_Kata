package com.hospital.patientmanagement.controller;

import com.hospital.patientmanagement.dto.ApiResponse;
import com.hospital.patientmanagement.dto.PatientRequest;
import com.hospital.patientmanagement.dto.PatientResponse;
import com.hospital.patientmanagement.enums.BloodGroup;
import com.hospital.patientmanagement.service.PatientService;
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
 * REST endpoints for patient profile management.
 *
 * <pre>
 * POST   /api/patients               — ADMIN | STAFF
 * PUT    /api/patients/{id}          — ADMIN | STAFF
 * GET    /api/patients/{id}          — ADMIN | STAFF | DOCTOR
 * GET    /api/patients               — ADMIN | STAFF | DOCTOR  (paginated + filtered)
 * PUT    /api/patients/{id}/activate   — ADMIN only
 * PUT    /api/patients/{id}/deactivate — ADMIN only
 * </pre>
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.createPatient(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient registered successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Patient retrieved", patientService.getPatientById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> getPatients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BloodGroup bloodGroup,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        Page<PatientResponse> page =
                patientService.getPatients(name, bloodGroup, activeOnly, pageable);
        return ResponseEntity.ok(ApiResponse.success("Patients retrieved", page));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PatientResponse>> activatePatient(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Patient activated", patientService.activatePatient(id)));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PatientResponse>> deactivatePatient(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Patient deactivated", patientService.deactivatePatient(id)));
    }
}
