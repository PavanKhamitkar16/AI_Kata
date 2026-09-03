package com.hospital.patientmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request body for {@code POST /api/appointments}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequest {

    @NotNull(message = "patientId is required")
    private UUID patientId;

    @NotNull(message = "doctorId is required")
    private UUID doctorId;

    @NotNull(message = "date is required")
    private LocalDate date;

    @NotNull(message = "startTime is required")
    private LocalTime startTime;

    @NotBlank(message = "reason is required")
    @Size(max = 500)
    private String reason;
}
