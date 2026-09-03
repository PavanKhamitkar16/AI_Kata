package com.hospital.patientmanagement.controller;

import com.hospital.patientmanagement.dto.ApiResponse;
import com.hospital.patientmanagement.dto.DoctorLeaveRequest;
import com.hospital.patientmanagement.dto.DoctorLeaveResponse;
import com.hospital.patientmanagement.entity.Doctor;
import com.hospital.patientmanagement.entity.DoctorLeave;
import com.hospital.patientmanagement.exception.ResourceNotFoundException;
import com.hospital.patientmanagement.repository.DoctorLeaveRepository;
import com.hospital.patientmanagement.repository.DoctorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST endpoints for doctor leave management.
 *
 * <pre>
 * POST   /api/doctors/{doctorId}/leave              — ADMIN only
 * GET    /api/doctors/{doctorId}/leave              — ADMIN | DOCTOR | STAFF
 * DELETE /api/doctors/{doctorId}/leave/{leaveId}    — ADMIN only
 * </pre>
 */
@RestController
@RequestMapping("/api/doctors/{doctorId}/leave")
@RequiredArgsConstructor
public class DoctorLeaveController {

    private final DoctorRepository     doctorRepository;
    private final DoctorLeaveRepository leaveRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorLeaveResponse>> addLeave(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorLeaveRequest request) {

        Doctor doctor = findDoctorOrThrow(doctorId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }

        DoctorLeave leave = DoctorLeave.builder()
                .doctor(doctor)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .build();

        DoctorLeave saved = leaveRepository.save(leave);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave added", DoctorLeaveResponse.from(saved)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public ResponseEntity<ApiResponse<List<DoctorLeaveResponse>>> getLeaves(
            @PathVariable UUID doctorId) {
        findDoctorOrThrow(doctorId);
        List<DoctorLeaveResponse> leaves = leaveRepository.findByDoctorId(doctorId)
                .stream().map(DoctorLeaveResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Leaves retrieved", leaves));
    }

    @DeleteMapping("/{leaveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLeave(
            @PathVariable UUID doctorId,
            @PathVariable UUID leaveId) {
        findDoctorOrThrow(doctorId);
        DoctorLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorLeave", "leaveId", leaveId));
        leaveRepository.delete(leave);
        return ResponseEntity.ok(ApiResponse.success("Leave deleted"));
    }

    private Doctor findDoctorOrThrow(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "doctorId", doctorId));
    }
}
