package com.hospital.patientmanagement.dto;

import lombok.*;

import java.time.LocalTime;

/**
 * A single available time slot returned by
 * {@code GET /api/doctors/{doctorId}/available-slots?date=YYYY-MM-DD}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotResponse {

    private LocalTime startTime;
    private LocalTime endTime;

    /** {@code true} when the slot is free; {@code false} when already booked. */
    private boolean   available;
}
