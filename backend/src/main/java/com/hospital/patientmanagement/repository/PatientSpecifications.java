package com.hospital.patientmanagement.repository;

import com.hospital.patientmanagement.entity.Patient;
import com.hospital.patientmanagement.enums.BloodGroup;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specification factories for {@link Patient} filtering.
 *
 * <p>Each factory returns {@code null} when the filter value is absent,
 * which Spring Data's {@code Specification.where(...).and(...)} chain
 * treats as "no restriction" — no WHERE clause is emitted for that filter.
 *
 * <p>This approach is the Hibernate 6 safe alternative to the JPQL
 * {@code :param IS NULL OR field = :param} pattern, which has known
 * type-inference regressions in Hibernate 6.4.x with PostgreSQL.
 */
public final class PatientSpecifications {

    private PatientSpecifications() {}

    /**
     * Case-insensitive partial match against the patient's full name
     * (firstName + " " + lastName).
     */
    public static Specification<Patient> hasName(String name) {
        if (name == null || name.isBlank()) return null;
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> {
            Expression<String> fullName = cb.lower(
                    cb.concat(
                            cb.concat(root.get("firstName"), " "),
                            root.get("lastName")));
            return cb.like(fullName, pattern);
        };
    }

    /** Exact match on {@code bloodGroup}. */
    public static Specification<Patient> hasBloodGroup(BloodGroup bloodGroup) {
        if (bloodGroup == null) return null;
        return (root, query, cb) -> cb.equal(root.get("bloodGroup"), bloodGroup);
    }

    /** Exact match on {@code isActive}. */
    public static Specification<Patient> isActive(Boolean activeOnly) {
        if (activeOnly == null) return null;
        return (root, query, cb) -> cb.equal(root.get("isActive"), activeOnly);
    }
}
