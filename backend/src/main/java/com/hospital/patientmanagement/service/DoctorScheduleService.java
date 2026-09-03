package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.DoctorScheduleRequest;
import com.hospital.patientmanagement.dto.DoctorScheduleResponse;
import com.hospital.patientmanagement.dto.TimeSlotResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Business operations for doctor working schedules.
 */
public interface DoctorScheduleService {

    /**
     * Replaces the doctor's entire schedule with the provided entries.
     * Any previous schedule rows for this doctor are removed first.
     */
    DoctorScheduleResponse saveSchedule(UUID doctorId, DoctorScheduleRequest request);

    /** Returns the current schedule for a doctor (may be empty). */
    DoctorScheduleResponse getSchedule(UUID doctorId);

    /**
     * Generates all possible time slots for a doctor on a given date, marking
     * each slot available or taken (booked / leave).
     *
     * @throws IllegalArgumentException if the doctor has no schedule for that day of week
     */
    List<TimeSlotResponse> getAvailableSlots(UUID doctorId, LocalDate date);
}
