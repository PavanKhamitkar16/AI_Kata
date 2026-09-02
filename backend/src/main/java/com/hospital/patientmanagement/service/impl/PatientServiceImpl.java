package com.hospital.patientmanagement.service.impl;

import com.hospital.patientmanagement.dto.PatientRequest;
import com.hospital.patientmanagement.dto.PatientResponse;
import com.hospital.patientmanagement.entity.Patient;
import com.hospital.patientmanagement.enums.BloodGroup;
import com.hospital.patientmanagement.exception.ResourceNotFoundException;
import com.hospital.patientmanagement.repository.PatientRepository;
import com.hospital.patientmanagement.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Default {@link PatientService} implementation.
 *
 * <p>All write operations run in a transaction; reads are marked
 * {@code readOnly} so the persistence provider can skip dirty-checking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    // ------------------------------------------------------------------ //
    //  Write operations
    // ------------------------------------------------------------------ //

    @Override
    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        if (StringUtils.hasText(request.getEmail())
                && patientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "A patient with email '" + request.getEmail() + "' already exists.");
        }

        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .emergencyContact(request.getEmergencyContact())
                .bloodGroup(request.getBloodGroup())
                .medicalHistory(request.getMedicalHistory())
                .allergies(request.getAllergies())
                .isActive(true)
                .build();

        Patient saved = patientRepository.save(patient);
        log.info("Patient created: id={}", saved.getPatientId());
        return PatientResponse.from(saved);
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(UUID id, PatientRequest request) {
        Patient patient = findOrThrow(id);

        // Guard against stealing another patient's email
        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equalsIgnoreCase(patient.getEmail())
                && patientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "A patient with email '" + request.getEmail() + "' already exists.");
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());

        Patient saved = patientRepository.save(patient);
        log.info("Patient updated: id={}", saved.getPatientId());
        return PatientResponse.from(saved);
    }

    @Override
    @Transactional
    public PatientResponse activatePatient(UUID id) {
        Patient patient = findOrThrow(id);
        patient.setIsActive(true);
        log.info("Patient activated: id={}", id);
        return PatientResponse.from(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public PatientResponse deactivatePatient(UUID id) {
        Patient patient = findOrThrow(id);
        patient.setIsActive(false);
        log.info("Patient deactivated: id={}", id);
        return PatientResponse.from(patientRepository.save(patient));
    }

    // ------------------------------------------------------------------ //
    //  Read operations
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID id) {
        return PatientResponse.from(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> getPatients(String name, BloodGroup bloodGroup,
                                              Boolean activeOnly, Pageable pageable) {
        return patientRepository
                .search(name, bloodGroup, activeOnly, pageable)
                .map(PatientResponse::from);
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    private Patient findOrThrow(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "patientId", id));
    }
}
