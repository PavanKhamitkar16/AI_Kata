package com.hospital.patientmanagement.dto;

import com.hospital.patientmanagement.entity.Patient;
import com.hospital.patientmanagement.enums.BloodGroup;
import com.hospital.patientmanagement.enums.Gender;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only projection returned by every patient endpoint.
 * Built from a {@link Patient} entity via the static factory {@link #from(Patient)}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {

    private UUID           patientId;
    private String         firstName;
    private String         lastName;
    private LocalDate      dateOfBirth;
    private Gender         gender;
    private String         phone;
    private String         email;
    private String         address;
    private String         emergencyContact;
    private BloodGroup     bloodGroup;
    private String         medicalHistory;
    private String         allergies;
    private Boolean        isActive;
    private LocalDateTime  createdAt;
    private LocalDateTime  updatedAt;

    /** Maps a {@link Patient} entity to this DTO without exposing entity references. */
    public static PatientResponse from(Patient patient) {
        return PatientResponse.builder()
                .patientId(patient.getPatientId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .emergencyContact(patient.getEmergencyContact())
                .bloodGroup(patient.getBloodGroup())
                .medicalHistory(patient.getMedicalHistory())
                .allergies(patient.getAllergies())
                .isActive(patient.getIsActive())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
