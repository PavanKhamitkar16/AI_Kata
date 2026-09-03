package com.hospital.patientmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * Request body for {@code POST /api/doctors/{doctorId}/schedule}.
 *
 * <p>Replaces the doctor's entire schedule in one call — the service
 * deletes existing entries and inserts the provided list.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorScheduleRequest {

    @NotEmpty(message = "At least one schedule entry is required")
    @Valid
    private List<ScheduleEntryRequest> entries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleEntryRequest {

        @jakarta.validation.constraints.NotNull(message = "dayOfWeek is required")
        private java.time.DayOfWeek dayOfWeek;

        @jakarta.validation.constraints.NotNull(message = "startTime is required")
        private java.time.LocalTime startTime;

        @jakarta.validation.constraints.NotNull(message = "endTime is required")
        private java.time.LocalTime endTime;

        @jakarta.validation.constraints.Positive(message = "slotDurationMinutes must be positive")
        private Integer slotDurationMinutes = 30;
    }
}
