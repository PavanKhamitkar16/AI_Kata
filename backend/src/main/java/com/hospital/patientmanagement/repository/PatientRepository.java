package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Patient}.
 *
 * <p>Dynamic filtering is handled via {@link JpaSpecificationExecutor} and
 * {@link PatientSpecifications}. This avoids the Hibernate 6.4.x regression
 * where JPQL {@code :param IS NULL OR field = :param} patterns fail to
 * correctly infer JDBC types for enum and Boolean parameters on PostgreSQL.
 */
public interface PatientRepository extends JpaRepository<Patient, UUID>,
        JpaSpecificationExecutor<Patient> {

    boolean existsByEmail(String email);
}
