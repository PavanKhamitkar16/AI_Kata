package com.hospital.patientmanagement.service.impl;

import com.hospital.patientmanagement.dto.DoctorRequest;
import com.hospital.patientmanagement.dto.DoctorResponse;
import com.hospital.patientmanagement.entity.Doctor;
import com.hospital.patientmanagement.enums.AvailabilityStatus;
import com.hospital.patientmanagement.exception.ResourceNotFoundException;
import com.hospital.patientmanagement.repository.DoctorRepository;
import com.hospital.patientmanagement.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default {@link DoctorService} implementation.
 *
 * <p>All write operations run in a transaction; reads are marked
 * {@code readOnly} so the persistence provider can skip dirty-checking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    // ------------------------------------------------------------------ //
    //  Write operations
    // ------------------------------------------------------------------ //

    @Override
    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "A doctor with email '" + request.getEmail() + "' already exists.");
        }

        Doctor doctor = Doctor.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .experienceYears(request.getExperienceYears())
                .department(request.getDepartment())
                .availabilityStatus(request.getAvailabilityStatus() != null
                        ? request.getAvailabilityStatus()
                        : AvailabilityStatus.AVAILABLE)
                .isActive(true)
                .build();

        Doctor saved = doctorRepository.save(doctor);
        log.info("Doctor created: id={}, email={}", saved.getDoctorId(), saved.getEmail());
        return DoctorResponse.from(saved);
    }

    @Override
    @Transactional
    public DoctorResponse updateDoctor(UUID id, DoctorRequest request) {
        Doctor doctor = findOrThrow(id);

        // Guard against stealing another doctor's email
        if (!doctor.getEmail().equalsIgnoreCase(request.getEmail())
                && doctorRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "A doctor with email '" + request.getEmail() + "' already exists.");
        }

        doctor.setName(request.getName());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setDepartment(request.getDepartment());
        if (request.getAvailabilityStatus() != null) {
            doctor.setAvailabilityStatus(request.getAvailabilityStatus());
        }

        Doctor saved = doctorRepository.save(doctor);
        log.info("Doctor updated: id={}", saved.getDoctorId());
        return DoctorResponse.from(saved);
    }

    @Override
    @Transactional
    public DoctorResponse activateDoctor(UUID id) {
        Doctor doctor = findOrThrow(id);
        doctor.setIsActive(true);
        log.info("Doctor activated: id={}", id);
        return DoctorResponse.from(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public DoctorResponse deactivateDoctor(UUID id) {
        Doctor doctor = findOrThrow(id);
        doctor.setIsActive(false);
        log.info("Doctor deactivated: id={}", id);
        return DoctorResponse.from(doctorRepository.save(doctor));
    }

    // ------------------------------------------------------------------ //
    //  Read operations
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID id) {
        return DoctorResponse.from(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getDoctors(String specialization, String department,
                                            AvailabilityStatus status, Boolean activeOnly,
                                            Pageable pageable) {
        return doctorRepository
                .search(specialization, department, status, activeOnly, pageable)
                .map(DoctorResponse::from);
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    private Doctor findOrThrow(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "doctorId", id));
    }
}
