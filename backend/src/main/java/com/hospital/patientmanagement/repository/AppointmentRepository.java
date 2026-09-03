package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.Appointment;
import com.hospital.patientmanagement.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for {@link Appointment}.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    /** All appointments for a patient, newest first. */
    @Query("SELECT a FROM Appointment a WHERE a.patient.patientId = :patientId ORDER BY a.appointmentDate DESC, a.startTime DESC")
    List<Appointment> findByPatientId(@Param("patientId") UUID patientId);

    /** All appointments for a doctor, newest first. */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.doctorId = :doctorId ORDER BY a.appointmentDate DESC, a.startTime DESC")
    List<Appointment> findByDoctorId(@Param("doctorId") UUID doctorId);

    /**
     * Returns true when a REQUESTED or CONFIRMED appointment already exists for the
     * given doctor on the given date starting at the given time (double-booking check).
     */
    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.doctorId = :doctorId
          AND a.appointmentDate  = :date
          AND a.startTime        = :startTime
          AND a.status IN :activeStatuses
        """)
    boolean isSlotTaken(@Param("doctorId")       UUID doctorId,
                        @Param("date")            LocalDate date,
                        @Param("startTime")       LocalTime startTime,
                        @Param("activeStatuses")  List<AppointmentStatus> activeStatuses);

    /**
     * All REQUESTED or CONFIRMED appointments for a doctor on a specific date —
     * used to subtract booked start-times when generating available slots.
     */
    @Query("""
        SELECT a.startTime FROM Appointment a
        WHERE a.doctor.doctorId = :doctorId
          AND a.appointmentDate  = :date
          AND a.status IN :activeStatuses
        """)
    List<LocalTime> findBookedStartTimes(@Param("doctorId")      UUID doctorId,
                                         @Param("date")           LocalDate date,
                                         @Param("activeStatuses") List<AppointmentStatus> activeStatuses);
}
