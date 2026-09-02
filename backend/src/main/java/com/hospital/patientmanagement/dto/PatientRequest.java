package com.hospital.patientmanagement.dto;

import com.hospital.patientmanagement.enums.BloodGroup;
import com.hospital.patientmanagement.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Request body for registering or updating a patient.
 * All required fields are validated before the controller delegates to the service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone must be 7-15 digits, optionally prefixed with +")
    private String phone;

    /** Optional — patients may not have an email address. */
    @Email(message = "Email must be a valid address")
    @Size(max = 100)
    private String email;

    private String address;

    @Size(max = 100, message = "Emergency contact must not exceed 100 characters")
    private String emergencyContact;

    /** Optional — may be unknown at registration time. */
    private BloodGroup bloodGroup;

    private String medicalHistory;

    private String allergies;
}
