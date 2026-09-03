package com.hospital.patientmanagement.service.impl;

import com.hospital.patientmanagement.dto.AppointmentRequest;
import com.hospital.patientmanagement.dto.AppointmentResponse;
import com.hospital.patientmanagement.dto.RescheduleRequest;
import com.hospital.patientmanagement.entity.Appointment;
import com.hospital.patientmanagement.entity.Doctor;
import com.hospital.patientmanagement.entity.DoctorSchedule;
import com.hospital.patientmanagement.entity.Patient;
import com.hospital.patientmanagement.enums.AppointmentStatus;
import com.hospital.patientmanagement.exception.ResourceNotFoundException;
import com.hospital.patientmanagement.repository.AppointmentRepository;
import com.hospital.patientmanagement.repository.DoctorLeaveRepository;
import com.hospital.patientmanagement.repository.DoctorRepository;
import com.hospital.patientmanagement.repository.DoctorScheduleRepository;
import com.hospital.patientmanagement.repository.PatientRepository;
import com.hospital.patientmanagement.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default {@link AppointmentService} implementation.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Slot must fall within the doctor's working schedule for that day of week.</li>
 *   <li>Doctor must not be on leave on the requested date.</li>
 *   <li>Slot must not already be taken by a REQUESTED or CONFIRMED appointment.</li>
 *   <li>endTime is calculated automatically from the schedule's slot duration.</li>
 *   <li>Status transitions are validated before every state change.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository    appointmentRepository;
    private final DoctorRepository         doctorRepository;
    private final PatientRepository        patientRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorLeaveRepository    leaveRepository;

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED);

    // ------------------------------------------------------------------ //
    //  createAppointment
    // ------------------------------------------------------------------ //

    @Override
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        Patient patient = findPatientOrThrow(request.getPatientId());
        Doctor  doctor  = findDoctorOrThrow(request.getDoctorId());
        LocalDate date  = request.getDate();
        LocalTime start = request.getStartTime();

        // 1. Doctor must have a schedule on that day of week
        DoctorSchedule schedule = scheduleRepository
                .findByDoctorIdAndDay(doctor.getDoctorId(), date.getDayOfWeek())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Doctor does not work on " + date.getDayOfWeek()));

        // 2. StartTime must be within working hours
        validateWithinWorkingHours(start, schedule);

        // 3. Doctor must not be on leave
        if (leaveRepository.isDoctorOnLeave(doctor.getDoctorId(), date)) {
            throw new IllegalArgumentException(
                    "Doctor is on leave on " + date + " — appointment cannot be booked.");
        }

        // 4. Slot must not be already taken
        if (appointmentRepository.isSlotTaken(doctor.getDoctorId(), date, start, ACTIVE_STATUSES)) {
            throw new IllegalArgumentException(
                    "The requested time slot is already booked: " + date + " " + start);
        }

        // 5. Calculate end time from schedule slot duration
        LocalTime end = start.plusMinutes(schedule.getSlotDurationMinutes());

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .startTime(start)
                .endTime(end)
                .reason(request.getReason())
                .status(AppointmentStatus.REQUESTED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment created: id={}, doctor={}, patient={}, date={} {}",
                saved.getAppointmentId(), doctor.getDoctorId(), patient.getPatientId(), date, start);
        return AppointmentResponse.from(saved);
    }

    // ------------------------------------------------------------------ //
    //  Read
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(UUID appointmentId) {
        return AppointmentResponse.from(findOrThrow(appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(UUID patientId) {
        findPatientOrThrow(patientId); // validate patient exists
        return appointmentRepository.findByPatientId(patientId)
                .stream().map(AppointmentResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(UUID doctorId) {
        findDoctorOrThrow(doctorId); // validate doctor exists
        return appointmentRepository.findByDoctorId(doctorId)
                .stream().map(AppointmentResponse::from).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Status transitions
    // ------------------------------------------------------------------ //

    @Override
    @Transactional
    public AppointmentResponse confirmAppointment(UUID appointmentId) {
        Appointment a = findOrThrow(appointmentId);
        requireStatus(a, AppointmentStatus.REQUESTED, "confirm");
        a.setStatus(AppointmentStatus.CONFIRMED);
        log.info("Appointment confirmed: id={}", appointmentId);
        return AppointmentResponse.from(appointmentRepository.save(a));
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId) {
        Appointment a = findOrThrow(appointmentId);
        if (a.getStatus() != AppointmentStatus.REQUESTED
                && a.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Only REQUESTED or CONFIRMED appointments can be cancelled. Current status: "
                            + a.getStatus());
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        log.info("Appointment cancelled: id={}", appointmentId);
        return AppointmentResponse.from(appointmentRepository.save(a));
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(UUID appointmentId, RescheduleRequest request) {
        Appointment a = findOrThrow(appointmentId);
        if (a.getStatus() != AppointmentStatus.REQUESTED
                && a.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Only REQUESTED or CONFIRMED appointments can be rescheduled. Current status: "
                            + a.getStatus());
        }

        LocalDate newDate  = request.getNewDate();
        LocalTime newStart = request.getNewStartTime();
        UUID doctorId      = a.getDoctor().getDoctorId();

        // Validate new slot
        DoctorSchedule schedule = scheduleRepository
                .findByDoctorIdAndDay(doctorId, newDate.getDayOfWeek())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Doctor does not work on " + newDate.getDayOfWeek()));

        validateWithinWorkingHours(newStart, schedule);

        if (leaveRepository.isDoctorOnLeave(doctorId, newDate)) {
            throw new IllegalArgumentException(
                    "Doctor is on leave on " + newDate + " — cannot reschedule.");
        }

        // Check double booking, excluding the current appointment's slot
        boolean slotTaken = appointmentRepository.isSlotTaken(doctorId, newDate, newStart, ACTIVE_STATUSES);
        boolean sameSlot  = a.getAppointmentDate().equals(newDate) && a.getStartTime().equals(newStart);
        if (slotTaken && !sameSlot) {
            throw new IllegalArgumentException(
                    "The new time slot is already booked: " + newDate + " " + newStart);
        }

        a.setAppointmentDate(newDate);
        a.setStartTime(newStart);
        a.setEndTime(newStart.plusMinutes(schedule.getSlotDurationMinutes()));
        a.setStatus(AppointmentStatus.REQUESTED); // Reset to REQUESTED after reschedule

        log.info("Appointment rescheduled: id={} → {} {}", appointmentId, newDate, newStart);
        return AppointmentResponse.from(appointmentRepository.save(a));
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(UUID appointmentId) {
        Appointment a = findOrThrow(appointmentId);
        requireStatus(a, AppointmentStatus.CONFIRMED, "complete");
        a.setStatus(AppointmentStatus.COMPLETED);
        log.info("Appointment completed: id={}", appointmentId);
        return AppointmentResponse.from(appointmentRepository.save(a));
    }

    @Override
    @Transactional
    public AppointmentResponse markNoShow(UUID appointmentId) {
        Appointment a = findOrThrow(appointmentId);
        requireStatus(a, AppointmentStatus.CONFIRMED, "mark as no-show");
        a.setStatus(AppointmentStatus.NO_SHOW);
        log.info("Appointment marked NO_SHOW: id={}", appointmentId);
        return AppointmentResponse.from(appointmentRepository.save(a));
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private Appointment findOrThrow(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "appointmentId", id));
    }

    private Doctor findDoctorOrThrow(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "doctorId", id));
    }

    private Patient findPatientOrThrow(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "patientId", id));
    }

    private void requireStatus(Appointment a, AppointmentStatus required, String action) {
        if (a.getStatus() != required) {
            throw new IllegalArgumentException(
                    "Cannot " + action + " an appointment in status " + a.getStatus()
                            + ". Required: " + required);
        }
    }

    private void validateWithinWorkingHours(LocalTime start, DoctorSchedule schedule) {
        LocalTime slotEnd = start.plusMinutes(schedule.getSlotDurationMinutes());
        if (start.isBefore(schedule.getStartTime()) || slotEnd.isAfter(schedule.getEndTime())) {
            throw new IllegalArgumentException(
                    "Requested time " + start + " is outside working hours "
                            + schedule.getStartTime() + "–" + schedule.getEndTime());
        }
    }
}
