package com.hospital.patientmanagement.entity;

import com.hospital.patientmanagement.enums.AvailabilityStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * Persistent doctor entity.
 *
 * <p>Extends {@link BaseEntity} for audit fields. Enum fields are stored
 * as VARCHAR so column values survive enum reordering.
 */
@Entity
@Table(
    name = "doctors",
    indexes = {
        @Index(name = "idx_doctors_email",          columnList = "email"),
        @Index(name = "idx_doctors_specialization", columnList = "specialization"),
        @Index(name = "idx_doctors_department",     columnList = "department"),
        @Index(name = "idx_doctors_status",         columnList = "availability_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doctor_id", updatable = false, nullable = false)
    private UUID doctorId;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String phone;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String specialization;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 20)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
