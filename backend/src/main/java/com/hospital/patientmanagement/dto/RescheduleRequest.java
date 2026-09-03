package com.hospital.patientmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request body for {@code PUT /api/appointments/{id}/reschedule}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleRequest {

    @NotNull(message = "newDate is required")
    private LocalDate newDate;

    @NotNull(message = "newStartTime is required")
    private LocalTime newStartTime;
}
