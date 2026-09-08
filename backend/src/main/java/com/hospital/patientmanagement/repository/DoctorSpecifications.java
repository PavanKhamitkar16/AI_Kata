package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.Doctor;
import com.hospital.patientmanagement.enums.AvailabilityStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specification factories for {@link Doctor} filtering.
 *
 * <p>Each factory returns {@code null} when the filter value is absent,
 * which Spring Data's {@code Specification.where(...).and(...)} chain
 * treats as "no restriction" — no WHERE clause is emitted for that filter.
 *
 * <p>This approach is the Hibernate 6 safe alternative to the JPQL
 * {@code :param IS NULL OR field = :param} pattern, which has known
 * type-inference regressions in Hibernate 6.4.x with PostgreSQL.
 */
public final class DoctorSpecifications {

    private DoctorSpecifications() {}

    /** Case-insensitive partial match on {@code specialization}. */
    public static Specification<Doctor> hasSpecialization(String specialization) {
        if (specialization == null || specialization.isBlank()) return null;
        String pattern = "%" + specialization.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("specialization")), pattern);
    }

    /** Case-insensitive partial match on {@code department}. */
    public static Specification<Doctor> hasDepartment(String department) {
        if (department == null || department.isBlank()) return null;
        String pattern = "%" + department.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("department")), pattern);
    }

    /** Exact match on {@code availabilityStatus}. */
    public static Specification<Doctor> hasStatus(AvailabilityStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("availabilityStatus"), status);
    }

    /** Exact match on {@code isActive}. */
    public static Specification<Doctor> isActive(Boolean activeOnly) {
        if (activeOnly == null) return null;
        return (root, query, cb) -> cb.equal(root.get("isActive"), activeOnly);
    }
}
