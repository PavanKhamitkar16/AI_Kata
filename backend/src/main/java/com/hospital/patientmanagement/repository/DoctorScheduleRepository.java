package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link DoctorSchedule}.
 */
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    /** All schedule entries for the given doctor, ordered by day of week name. */
    @Query("SELECT s FROM DoctorSchedule s WHERE s.doctor.doctorId = :doctorId ORDER BY s.dayOfWeek")
    List<DoctorSchedule> findByDoctorId(@Param("doctorId") UUID doctorId);

    /** Schedule entry for a specific doctor + day combination. */
    @Query("SELECT s FROM DoctorSchedule s WHERE s.doctor.doctorId = :doctorId AND s.dayOfWeek = :day")
    Optional<DoctorSchedule> findByDoctorIdAndDay(@Param("doctorId") UUID doctorId,
                                                   @Param("day") DayOfWeek day);

    /** Remove all schedule entries for a doctor (used when replacing the full schedule). */
    void deleteByDoctorDoctorId(UUID doctorId);
}
