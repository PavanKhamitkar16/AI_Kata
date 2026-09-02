package com.hospital.patientmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Provides the currently authenticated username to Spring Data's auditing
 * infrastructure so that {@code createdBy} and {@code updatedBy} on
 * {@link com.hospital.patientmanagement.entity.BaseEntity} are populated
 * automatically.
 *
 * <p>Falls back to "system" for unauthenticated operations such as
 * data-seeding on startup.
 */
@Configuration
public class AuditorAwareConfig {

    @Bean(name = "auditorAware")
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
}
