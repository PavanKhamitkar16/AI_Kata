package com.hospital.patientmanagement.dto;

import com.hospital.patientmanagement.entity.Appointment;
import com.hospital.patientmanagement.enums.AppointmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Read-only projection returned by every appointment endpoint.
 * Built from an {@link Appointment} entity via the static factory {@link #from(Appointment)}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private UUID              appointmentId;
    private UUID              patientId;
    private String            patientName;
    private UUID              doctorId;
    private String            doctorName;
    private String            doctorSpecialization;
    private LocalDate         appointmentDate;
    private LocalTime         startTime;
    private LocalTime         endTime;
    private String            reason;
    private AppointmentStatus status;
    private String            notes;
    private LocalDateTime     createdAt;
    private LocalDateTime     updatedAt;

    public static AppointmentResponse from(Appointment a) {
        String patientName = a.getPatient().getFirstName() + " " + a.getPatient().getLastName();
        return AppointmentResponse.builder()
                .appointmentId(a.getAppointmentId())
                .patientId(a.getPatient().getPatientId())
                .patientName(patientName)
                .doctorId(a.getDoctor().getDoctorId())
                .doctorName(a.getDoctor().getName())
                .doctorSpecialization(a.getDoctor().getSpecialization())
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .reason(a.getReason())
                .status(a.getStatus())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
