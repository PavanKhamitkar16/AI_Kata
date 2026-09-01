package com.hospital.patientmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the Hospital Patient Management System backend.
 *
 * <p>{@code @EnableJpaAuditing} activates the Spring Data auditing infrastructure
 * that populates {@code createdAt}, {@code updatedAt}, {@code createdBy}, and
 * {@code updatedBy} on every {@link com.hospital.patientmanagement.entity.BaseEntity}.
 * The bean name "auditorAware" must match the one registered in
 * {@link com.hospital.patientmanagement.config.AuditorAwareConfig}.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class PatientManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientManagementApplication.class, args);
    }
}
