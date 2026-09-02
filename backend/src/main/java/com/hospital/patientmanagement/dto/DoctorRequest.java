package com.hospital.patientmanagement.dto;

import com.hospital.patientmanagement.enums.AvailabilityStatus;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request body for creating or updating a doctor.
 * All fields are validated before the controller delegates to the service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone must be 7-15 digits, optionally prefixed with +")
    private String phone;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;

    @NotBlank(message = "Qualification is required")
    @Size(max = 200, message = "Qualification must not exceed 200 characters")
    private String qualification;

    @Min(value = 0,  message = "Experience years must be non-negative")
    @Max(value = 60, message = "Experience years must be at most 60")
    private Integer experienceYears;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    /** Optional — defaults to AVAILABLE in the service if not supplied. */
    private AvailabilityStatus availabilityStatus;
}
