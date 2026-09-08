package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.Doctor;
import com.hospital.patientmanagement.enums.AvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Doctor}.
 *
 * <p>The {@code search} JPQL query supports optional filters: passing {@code null}
 * for any parameter skips that filter clause, giving callers full flexibility.
 */
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    boolean existsByEmail(String email);

    java.util.Optional<Doctor> findByEmail(String email);

    /**
     * Filtered, paginated doctor list.
     * Any parameter that is {@code null} is excluded from the WHERE clause.
     */
    @Query("""
            SELECT d FROM Doctor d
            WHERE (:specialization IS NULL
                   OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')))
              AND (:department IS NULL
                   OR LOWER(d.department) LIKE LOWER(CONCAT('%', :department, '%')))
              AND (:status IS NULL
                   OR d.availabilityStatus = :status)
              AND (:activeOnly IS NULL
                   OR d.isActive = :activeOnly)
            """)
    Page<Doctor> search(
            @Param("specialization") String specialization,
            @Param("department")     String department,
            @Param("status")         AvailabilityStatus status,
            @Param("activeOnly")     Boolean activeOnly,
            Pageable pageable);
}
