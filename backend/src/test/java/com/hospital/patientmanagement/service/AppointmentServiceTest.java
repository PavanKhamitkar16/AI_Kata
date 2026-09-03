package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.AppointmentRequest;
import com.hospital.patientmanagement.dto.AppointmentResponse;
import com.hospital.patientmanagement.dto.RescheduleRequest;
import com.hospital.patientmanagement.entity.*;
import com.hospital.patientmanagement.enums.AppointmentStatus;
import com.hospital.patientmanagement.exception.ResourceNotFoundException;
import com.hospital.patientmanagement.repository.*;
import com.hospital.patientmanagement.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AppointmentServiceImpl}.
 *
 * <p>Plain Mockito — no Spring context. All tests run in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit Tests")
class AppointmentServiceTest {

    @Mock private AppointmentRepository    appointmentRepository;
    @Mock private DoctorRepository         doctorRepository;
    @Mock private PatientRepository        patientRepository;
    @Mock private DoctorScheduleRepository scheduleRepository;
    @Mock private DoctorLeaveRepository    leaveRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    // ------------------------------------------------------------------ //
    //  Shared fixtures
    // ------------------------------------------------------------------ //

    private UUID           doctorId;
    private UUID           patientId;
    private UUID           appointmentId;
    private Doctor         doctor;
    private Patient        patient;
    private DoctorSchedule mondaySchedule;
    private AppointmentRequest validRequest;

    /** Monday 2026-09-07, slot 09:00–09:30 */
    private static final LocalDate  DATE_MONDAY = LocalDate.of(2026, 9, 7);
    private static final LocalTime  START_09_00 = LocalTime.of(9, 0);
    private static final LocalTime  START_09_30 = LocalTime.of(9, 30);
    private static final LocalTime  END_17_00   = LocalTime.of(17, 0);

    @BeforeEach
    void setUp() {
        doctorId      = UUID.randomUUID();
        patientId     = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        doctor = Doctor.builder()
                .doctorId(doctorId)
                .name("Dr. Smith")
                .email("smith@hospital.com")
                .phone("1234567890")
                .specialization("Cardiology")
                .qualification("MD")
                .department("Cardiac")
                .isActive(true)
                .build();

        patient = Patient.builder()
                .patientId(patientId)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phone("0987654321")
                .isActive(true)
                .build();

        mondaySchedule = DoctorSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(START_09_00)
                .endTime(END_17_00)
                .slotDurationMinutes(30)
                .build();

        validRequest = AppointmentRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .date(DATE_MONDAY)
                .startTime(START_09_00)
                .reason("Chest pain")
                .build();
    }

    // ------------------------------------------------------------------ //
    //  createAppointment — happy path
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createAppointment — valid request — returns REQUESTED appointment")
    void createAppointment_validRequest_returnsRequestedAppointment() {
        // Arrange
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDay(doctorId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(mondaySchedule));
        when(leaveRepository.isDoctorOnLeave(doctorId, DATE_MONDAY)).thenReturn(false);
        when(appointmentRepository.isSlotTaken(eq(doctorId), eq(DATE_MONDAY), eq(START_09_00), anyList()))
                .thenReturn(false);

        Appointment saved = buildAppointment(AppointmentStatus.REQUESTED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);

        // Act
        AppointmentResponse response = appointmentService.createAppointment(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.REQUESTED);
        assertThat(response.getAppointmentDate()).isEqualTo(DATE_MONDAY);
        assertThat(response.getStartTime()).isEqualTo(START_09_00);
        assertThat(response.getEndTime()).isEqualTo(START_09_30);

        verify(appointmentRepository).save(any(Appointment.class));
    }

    // ------------------------------------------------------------------ //
    //  createAppointment — double-booking prevention
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createAppointment — slot already taken — throws IllegalArgumentException")
    void createAppointment_slotAlreadyTaken_throwsIllegalArgumentException() {
        // Arrange
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDay(doctorId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(mondaySchedule));
        when(leaveRepository.isDoctorOnLeave(doctorId, DATE_MONDAY)).thenReturn(false);
        when(appointmentRepository.isSlotTaken(eq(doctorId), eq(DATE_MONDAY), eq(START_09_00), anyList()))
                .thenReturn(true); // slot is taken

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already booked");

        verify(appointmentRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  createAppointment — leave-day prevention
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createAppointment — doctor on leave — throws IllegalArgumentException")
    void createAppointment_doctorOnLeave_throwsIllegalArgumentException() {
        // Arrange
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDay(doctorId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(mondaySchedule));
        when(leaveRepository.isDoctorOnLeave(doctorId, DATE_MONDAY)).thenReturn(true); // on leave

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leave");

        verify(appointmentRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  createAppointment — outside working hours
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createAppointment — startTime outside working hours — throws IllegalArgumentException")
    void createAppointment_outsideWorkingHours_throwsIllegalArgumentException() {
        // Request for 08:00 — before schedule start of 09:00
        AppointmentRequest earlyRequest = AppointmentRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .date(DATE_MONDAY)
                .startTime(LocalTime.of(8, 0))
                .reason("Early bird")
                .build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDay(doctorId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(mondaySchedule));

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(earlyRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside working hours");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAppointment — startTime would overflow past end-of-day — throws IllegalArgumentException")
    void createAppointment_slotOverflowsPastEndTime_throwsIllegalArgumentException() {
        // Request for 16:45 with 30-min slots — end would be 17:15, past 17:00
        AppointmentRequest lateRequest = AppointmentRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .date(DATE_MONDAY)
                .startTime(LocalTime.of(16, 45))
                .reason("Late slot")
                .build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDay(doctorId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(mondaySchedule));

        assertThatThrownBy(() -> appointmentService.createAppointment(lateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside working hours");

        verify(appointmentRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  createAppointment — no schedule for that day
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createAppointment — doctor has no schedule on that day — throws IllegalArgumentException")
    void createAppointment_noScheduleForDay_throwsIllegalArgumentException() {
        // Use a Sunday (2026-09-06) — no schedule entry
        AppointmentRequest sundayRequest = AppointmentRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .date(LocalDate.of(2026, 9, 6)) // Sunday
                .startTime(START_09_00)
                .reason("Weekend check")
                .build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDay(doctorId, DayOfWeek.SUNDAY))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(sundayRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not work on");

        verify(appointmentRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  Status transitions
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("confirmAppointment — REQUESTED → CONFIRMED")
    void confirmAppointment_fromRequested_becomesConfirmed() {
        Appointment a = buildAppointment(AppointmentStatus.REQUESTED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.confirmAppointment(appointmentId);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).save(argThat(ap -> ap.getStatus() == AppointmentStatus.CONFIRMED));
    }

    @Test
    @DisplayName("confirmAppointment — already CONFIRMED — throws IllegalArgumentException")
    void confirmAppointment_alreadyConfirmed_throwsIllegalArgumentException() {
        Appointment a = buildAppointment(AppointmentStatus.CONFIRMED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> appointmentService.confirmAppointment(appointmentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CONFIRMED");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelAppointment — REQUESTED → CANCELLED")
    void cancelAppointment_fromRequested_becomesCancelled() {
        Appointment a = buildAppointment(AppointmentStatus.REQUESTED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelAppointment — CONFIRMED → CANCELLED")
    void cancelAppointment_fromConfirmed_becomesCancelled() {
        Appointment a = buildAppointment(AppointmentStatus.CONFIRMED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelAppointment — COMPLETED — throws IllegalArgumentException")
    void cancelAppointment_completed_throwsIllegalArgumentException() {
        Appointment a = buildAppointment(AppointmentStatus.COMPLETED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(appointmentId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("completeAppointment — CONFIRMED → COMPLETED")
    void completeAppointment_fromConfirmed_becomesCompleted() {
        Appointment a = buildAppointment(AppointmentStatus.CONFIRMED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.completeAppointment(appointmentId);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    @DisplayName("completeAppointment — REQUESTED — throws IllegalArgumentException")
    void completeAppointment_fromRequested_throwsIllegalArgumentException() {
        Appointment a = buildAppointment(AppointmentStatus.REQUESTED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> appointmentService.completeAppointment(appointmentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CONFIRMED");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("markNoShow — CONFIRMED → NO_SHOW")
    void markNoShow_fromConfirmed_becomesNoShow() {
        Appointment a = buildAppointment(AppointmentStatus.CONFIRMED, DATE_MONDAY, START_09_00);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.markNoShow(appointmentId);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
    }

    // ------------------------------------------------------------------ //
    //  rescheduleAppointment
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("rescheduleAppointment — valid new slot — appointment moved")
    void rescheduleAppointment_validNewSlot_appointmentMoved() {
        Appointment a = buildAppointment(AppointmentStatus.REQUESTED, DATE_MONDAY, START_09_00);
        LocalDate newDate  = LocalDate.of(2026, 9, 14); // Also a Monday
        LocalTime newStart = LocalTime.of(10, 0);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));
        when(scheduleRepository.findByDoctorIdAndDay(doctorId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(mondaySchedule));
        when(leaveRepository.isDoctorOnLeave(doctorId, newDate)).thenReturn(false);
        when(appointmentRepository.isSlotTaken(eq(doctorId), eq(newDate), eq(newStart), anyList()))
                .thenReturn(false);
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RescheduleRequest req = RescheduleRequest.builder()
                .newDate(newDate)
                .newStartTime(newStart)
                .build();

        AppointmentResponse response = appointmentService.rescheduleAppointment(appointmentId, req);

        assertThat(response.getAppointmentDate()).isEqualTo(newDate);
        assertThat(response.getStartTime()).isEqualTo(newStart);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.REQUESTED);
    }

    @Test
    @DisplayName("rescheduleAppointment — new slot already taken — throws IllegalArgumentException")
    void rescheduleAppointment_newSlotTaken_throwsIllegalArgumentException() {
        Appointment a = buildAppointment(AppointmentStatus.REQUESTED, DATE_MONDAY, START_09_00);
        LocalDate newDate  = LocalDate.of(2026, 9, 14);
        LocalTime newStart = LocalTime.of(10, 0);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(a));
        when(scheduleRepository.findByDoctorIdAndDay(doctorId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(mondaySchedule));
        when(leaveRepository.isDoctorOnLeave(doctorId, newDate)).thenReturn(false);
        when(appointmentRepository.isSlotTaken(eq(doctorId), eq(newDate), eq(newStart), anyList()))
                .thenReturn(true); // new slot is taken

        RescheduleRequest req = new RescheduleRequest(newDate, newStart);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(appointmentId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already booked");

        verify(appointmentRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  getAppointment — not found
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getAppointment — unknown id — throws ResourceNotFoundException")
    void getAppointment_unknownId_throwsResourceNotFoundException() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointment(appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment");
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    private Appointment buildAppointment(AppointmentStatus status, LocalDate date, LocalTime start) {
        return Appointment.builder()
                .appointmentId(appointmentId)
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .startTime(start)
                .endTime(start.plusMinutes(30))
                .reason("Test reason")
                .status(status)
                .build();
    }
}
