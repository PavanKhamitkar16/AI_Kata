package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Doctor}.
 *
 * <p>Dynamic filtering is handled via {@link JpaSpecificationExecutor} and
 * {@link DoctorSpecifications}. This avoids the Hibernate 6.4.x regression
 * where JPQL {@code :param IS NULL OR field = :param} patterns fail to
 * correctly infer JDBC types for enum and Boolean parameters on PostgreSQL.
 */
public interface DoctorRepository extends JpaRepository<Doctor, UUID>,
        JpaSpecificationExecutor<Doctor> {

    boolean existsByEmail(String email);

    java.util.Optional<Doctor> findByEmail(String email);
}
