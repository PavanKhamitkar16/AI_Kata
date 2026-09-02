package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.DoctorRequest;
import com.hospital.patientmanagement.dto.DoctorResponse;
import com.hospital.patientmanagement.enums.AvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Business operations for doctor profile management.
 */
public interface DoctorService {

    /** Creates a new doctor. Throws {@link IllegalArgumentException} if the email is taken. */
    DoctorResponse createDoctor(DoctorRequest request);

    /** Updates an existing doctor. Throws {@link com.hospital.patientmanagement.exception.ResourceNotFoundException} if not found. */
    DoctorResponse updateDoctor(UUID id, DoctorRequest request);

    /** Returns a single doctor or throws {@link com.hospital.patientmanagement.exception.ResourceNotFoundException}. */
    DoctorResponse getDoctorById(UUID id);

    /** Returns a filtered, paginated list of doctors. Any filter param may be {@code null} to skip it. */
    Page<DoctorResponse> getDoctors(String specialization, String department,
                                    AvailabilityStatus status, Boolean activeOnly,
                                    Pageable pageable);

    /** Sets {@code isActive = true}. */
    DoctorResponse activateDoctor(UUID id);

    /** Sets {@code isActive = false}. */
    DoctorResponse deactivateDoctor(UUID id);
}
