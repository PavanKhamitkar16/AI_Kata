package com.hospital.patientmanagement.dto;

import com.hospital.patientmanagement.entity.DoctorSchedule;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO for doctor schedule endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorScheduleResponse {

    private UUID             doctorId;
    private List<EntryDto>   entries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EntryDto {
        private UUID       scheduleId;
        private DayOfWeek  dayOfWeek;
        private LocalTime  startTime;
        private LocalTime  endTime;
        private Integer    slotDurationMinutes;

        public static EntryDto from(DoctorSchedule s) {
            return EntryDto.builder()
                    .scheduleId(s.getScheduleId())
                    .dayOfWeek(s.getDayOfWeek())
                    .startTime(s.getStartTime())
                    .endTime(s.getEndTime())
                    .slotDurationMinutes(s.getSlotDurationMinutes())
                    .build();
        }
    }

    public static DoctorScheduleResponse from(UUID doctorId, List<DoctorSchedule> schedules) {
        return DoctorScheduleResponse.builder()
                .doctorId(doctorId)
                .entries(schedules.stream().map(EntryDto::from).collect(Collectors.toList()))
                .build();
    }
}
