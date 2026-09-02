package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.PatientRequest;
import com.hospital.patientmanagement.dto.PatientResponse;
import com.hospital.patientmanagement.enums.BloodGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Business operations for patient profile management.
 */
public interface PatientService {

    /** Registers a new patient. Throws {@link IllegalArgumentException} if the email is already taken. */
    PatientResponse createPatient(PatientRequest request);

    /** Updates an existing patient. Throws {@link com.hospital.patientmanagement.exception.ResourceNotFoundException} if not found. */
    PatientResponse updatePatient(UUID id, PatientRequest request);

    /** Returns a single patient or throws {@link com.hospital.patientmanagement.exception.ResourceNotFoundException}. */
    PatientResponse getPatientById(UUID id);

    /** Returns a filtered, paginated list of patients. Any filter param may be {@code null} to skip it. */
    Page<PatientResponse> getPatients(String name, BloodGroup bloodGroup,
                                      Boolean activeOnly, Pageable pageable);

    /** Sets {@code isActive = true}. */
    PatientResponse activatePatient(UUID id);

    /** Sets {@code isActive = false}. */
    PatientResponse deactivatePatient(UUID id);
}
