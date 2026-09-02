package com.hospital.patientmanagement.config;

import com.hospital.patientmanagement.entity.User;
import com.hospital.patientmanagement.enums.Role;
import com.hospital.patientmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the default admin user on first startup if it does not exist.
 *
 * <p>Using a {@link CommandLineRunner} rather than a Flyway SQL INSERT keeps
 * the password hash tied to the application's BCrypt cost factor, avoiding
 * out-of-sync hashes if the factor is ever changed.
 *
 * <p><strong>Change the default password immediately after first login.</strong>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@hospital.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .firstName("System")
                    .lastName("Admin")
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created — username: admin | CHANGE THIS PASSWORD.");
        } else {
            log.debug("Admin user already exists — skipping seed.");
        }
    }
}
