package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.Patient;
import com.hospital.patientmanagement.enums.BloodGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Patient}.
 *
 * <p>The {@code search} JPQL query supports optional filters: passing {@code null}
 * for any parameter skips that filter clause.
 */
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    boolean existsByEmail(String email);

    /**
     * Filtered, paginated patient list.
     * Name filter matches against the concatenation of firstName and lastName.
     * Any parameter that is {@code null} is excluded from the WHERE clause.
     */
    @Query("""
            SELECT p FROM Patient p
            WHERE (:name IS NULL
                   OR LOWER(CONCAT(p.firstName, ' ', p.lastName))
                      LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:bloodGroup IS NULL
                   OR p.bloodGroup = :bloodGroup)
              AND (:activeOnly IS NULL
                   OR p.isActive = :activeOnly)
            """)
    Page<Patient> search(
            @Param("name")       String name,
            @Param("bloodGroup") BloodGroup bloodGroup,
            @Param("activeOnly") Boolean activeOnly,
            Pageable pageable);
}
