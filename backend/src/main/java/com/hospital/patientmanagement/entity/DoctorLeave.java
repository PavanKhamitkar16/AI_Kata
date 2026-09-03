package com.hospital.patientmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A date range during which a doctor is unavailable (leave / holiday / absence).
 *
 * <p>Appointments cannot be created on any date that falls within an active leave
 * range for the given doctor. This check is enforced in {@code AppointmentServiceImpl}.
 */
@Entity
@Table(
    name = "doctor_leaves",
    indexes = {
        @Index(name = "idx_doctor_leaves_doctor_id",  columnList = "doctor_id"),
        @Index(name = "idx_doctor_leaves_date_range", columnList = "doctor_id, start_date, end_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorLeave extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "leave_id", updatable = false, nullable = false)
    private UUID leaveId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Size(max = 500)
    @Column(length = 500)
    private String reason;
}
