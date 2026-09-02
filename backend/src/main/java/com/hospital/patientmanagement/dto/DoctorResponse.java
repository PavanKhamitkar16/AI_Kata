package com.hospital.patientmanagement.dto;

import com.hospital.patientmanagement.entity.Doctor;
import com.hospital.patientmanagement.enums.AvailabilityStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only projection returned by every doctor endpoint.
 * Built from a {@link Doctor} entity via the static factory {@link #from(Doctor)}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    private UUID               doctorId;
    private String             name;
    private String             email;
    private String             phone;
    private String             specialization;
    private String             qualification;
    private Integer            experienceYears;
    private String             department;
    private AvailabilityStatus availabilityStatus;
    private Boolean            isActive;
    private LocalDateTime      createdAt;
    private LocalDateTime      updatedAt;

    /** Maps a {@link Doctor} entity to this DTO without exposing entity references. */
    public static DoctorResponse from(Doctor doctor) {
        return DoctorResponse.builder()
                .doctorId(doctor.getDoctorId())
                .name(doctor.getName())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experienceYears(doctor.getExperienceYears())
                .department(doctor.getDepartment())
                .availabilityStatus(doctor.getAvailabilityStatus())
                .isActive(doctor.getIsActive())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}
