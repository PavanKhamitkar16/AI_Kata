package com.hospital.patientmanagement.dto;

import com.hospital.patientmanagement.entity.DoctorLeave;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for doctor leave endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorLeaveResponse {

    private UUID          leaveId;
    private UUID          doctorId;
    private LocalDate     startDate;
    private LocalDate     endDate;
    private String        reason;
    private LocalDateTime createdAt;

    public static DoctorLeaveResponse from(DoctorLeave leave) {
        return DoctorLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .doctorId(leave.getDoctor().getDoctorId())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .reason(leave.getReason())
                .createdAt(leave.getCreatedAt())
                .build();
    }
}
