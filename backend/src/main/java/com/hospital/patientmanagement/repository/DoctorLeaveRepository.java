package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for {@link DoctorLeave}.
 */
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, UUID> {

    /** All leave entries for a doctor, ordered by start date. */
    @Query("SELECT l FROM DoctorLeave l WHERE l.doctor.doctorId = :doctorId ORDER BY l.startDate")
    List<DoctorLeave> findByDoctorId(@Param("doctorId") UUID doctorId);

    /**
     * Returns true if the doctor has any leave entry whose range overlaps the given date.
     * A date {@code d} is covered when {@code startDate <= d <= endDate}.
     */
    @Query("""
        SELECT COUNT(l) > 0 FROM DoctorLeave l
        WHERE l.doctor.doctorId = :doctorId
          AND l.startDate <= :date
          AND l.endDate   >= :date
        """)
    boolean isDoctorOnLeave(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date);
}
