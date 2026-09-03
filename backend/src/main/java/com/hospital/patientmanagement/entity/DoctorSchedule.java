package com.hospital.patientmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Working-schedule entry for a doctor on a given day of the week.
 *
 * <p>One row per (doctor, dayOfWeek) pair — enforced by the {@code uq_doctor_day}
 * unique constraint in the migration. Slot generation is handled in the service
 * layer using {@code startTime}, {@code endTime}, and {@code slotDurationMinutes}.
 */
@Entity
@Table(
    name = "doctor_schedules",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_doctor_day",
        columnNames = {"doctor_id", "day_of_week"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_id", updatable = false, nullable = false)
    private UUID scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Positive
    @Column(name = "slot_duration_minutes", nullable = false)
    @Builder.Default
    private Integer slotDurationMinutes = 30;
}
