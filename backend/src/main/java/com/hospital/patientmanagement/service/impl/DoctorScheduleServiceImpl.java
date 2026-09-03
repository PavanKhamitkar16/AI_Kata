package com.hospital.patientmanagement.service.impl;

import com.hospital.patientmanagement.dto.DoctorScheduleRequest;
import com.hospital.patientmanagement.dto.DoctorScheduleResponse;
import com.hospital.patientmanagement.dto.TimeSlotResponse;
import com.hospital.patientmanagement.entity.Doctor;
import com.hospital.patientmanagement.entity.DoctorSchedule;
import com.hospital.patientmanagement.enums.AppointmentStatus;
import com.hospital.patientmanagement.exception.ResourceNotFoundException;
import com.hospital.patientmanagement.repository.AppointmentRepository;
import com.hospital.patientmanagement.repository.DoctorLeaveRepository;
import com.hospital.patientmanagement.repository.DoctorRepository;
import com.hospital.patientmanagement.repository.DoctorScheduleRepository;
import com.hospital.patientmanagement.service.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default {@link DoctorScheduleService} implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorRepository         doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorLeaveRepository    leaveRepository;
    private final AppointmentRepository    appointmentRepository;

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED);

    // ------------------------------------------------------------------ //
    //  saveSchedule
    // ------------------------------------------------------------------ //

    @Override
    @Transactional
    public DoctorScheduleResponse saveSchedule(UUID doctorId, DoctorScheduleRequest request) {
        Doctor doctor = findDoctorOrThrow(doctorId);

        // Validate: end > start for every entry
        for (DoctorScheduleRequest.ScheduleEntryRequest entry : request.getEntries()) {
            if (!entry.getEndTime().isAfter(entry.getStartTime())) {
                throw new IllegalArgumentException(
                        "endTime must be after startTime for " + entry.getDayOfWeek());
            }
        }

        // Replace existing schedule
        scheduleRepository.deleteByDoctorDoctorId(doctorId);

        List<DoctorSchedule> saved = request.getEntries().stream()
                .map(e -> scheduleRepository.save(DoctorSchedule.builder()
                        .doctor(doctor)
                        .dayOfWeek(e.getDayOfWeek())
                        .startTime(e.getStartTime())
                        .endTime(e.getEndTime())
                        .slotDurationMinutes(e.getSlotDurationMinutes() != null
                                ? e.getSlotDurationMinutes() : 30)
                        .build()))
                .collect(Collectors.toList());

        log.info("Schedule saved for doctorId={}, {} entries", doctorId, saved.size());
        return DoctorScheduleResponse.from(doctorId, saved);
    }

    // ------------------------------------------------------------------ //
    //  getSchedule
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public DoctorScheduleResponse getSchedule(UUID doctorId) {
        findDoctorOrThrow(doctorId); // validates doctor exists
        List<DoctorSchedule> entries = scheduleRepository.findByDoctorId(doctorId);
        return DoctorScheduleResponse.from(doctorId, entries);
    }

    // ------------------------------------------------------------------ //
    //  getAvailableSlots
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(UUID doctorId, LocalDate date) {
        findDoctorOrThrow(doctorId);

        // Fetch schedule entry for this day of week
        DoctorSchedule schedule = scheduleRepository
                .findByDoctorIdAndDay(doctorId, date.getDayOfWeek())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Doctor has no working schedule on " + date.getDayOfWeek()));

        // Determine whether the doctor is on leave that day
        boolean onLeave = leaveRepository.isDoctorOnLeave(doctorId, date);

        // Get already-booked start times
        Set<LocalTime> bookedTimes = appointmentRepository
                .findBookedStartTimes(doctorId, date, ACTIVE_STATUSES)
                .stream()
                .collect(Collectors.toSet());

        // Generate slots
        List<TimeSlotResponse> slots = new ArrayList<>();
        LocalTime cursor = schedule.getStartTime();
        int duration = schedule.getSlotDurationMinutes();

        while (cursor.plusMinutes(duration).compareTo(schedule.getEndTime()) <= 0) {
            LocalTime slotEnd = cursor.plusMinutes(duration);
            boolean available = !onLeave && !bookedTimes.contains(cursor);
            slots.add(TimeSlotResponse.builder()
                    .startTime(cursor)
                    .endTime(slotEnd)
                    .available(available)
                    .build());
            cursor = slotEnd;
        }

        return slots;
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    private Doctor findDoctorOrThrow(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "doctorId", doctorId));
    }
}
