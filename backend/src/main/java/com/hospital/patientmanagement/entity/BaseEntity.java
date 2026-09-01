package com.hospital.patientmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base JPA entity with standard audit fields.
 *
 * <p>All entities that extend this class automatically get:
 * <ul>
 *   <li>{@code createdAt}  — set once on INSERT</li>
 *   <li>{@code updatedAt}  — updated on every UPDATE</li>
 *   <li>{@code createdBy}  — username of the authenticated principal at INSERT time</li>
 *   <li>{@code updatedBy}  — username of the authenticated principal at last UPDATE time</li>
 * </ul>
 *
 * Requires {@code @EnableJpaAuditing} on the application class and an
 * {@code AuditorAware<String>} bean wired to Spring Security's context.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
