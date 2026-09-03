package com.hospital.patientmanagement.entity;

import com.hospital.patientmanagement.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * An appointment between a {@link Patient} and a {@link Doctor}.
 *
 * <p>The (doctor_id, appointment_date, start_time) triple is unique — enforced by
 * {@code uq_doctor_slot} in the migration — preventing double-booking at the DB level.
 * The service layer additionally validates business rules before persisting.
 */
@Entity
@Table(
    name = "appointments",
    indexes = {
        @Index(name = "idx_appointments_patient_id",  columnList = "patient_id"),
        @Index(name = "idx_appointments_doctor_id",   columnList = "doctor_id"),
        @Index(name = "idx_appointments_date",        columnList = "appointment_date"),
        @Index(name = "idx_appointments_status",      columnList = "status"),
        @Index(name = "idx_appointments_doctor_date", columnList = "doctor_id, appointment_date")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uq_doctor_slot",
        columnNames = {"doctor_id", "appointment_date", "start_time"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id", updatable = false, nullable = false)
    private UUID appointmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.REQUESTED;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
