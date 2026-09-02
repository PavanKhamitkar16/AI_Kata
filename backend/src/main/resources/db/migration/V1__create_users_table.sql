-- ============================================================
-- V1 — Create users table
-- ============================================================
-- Flyway runs this exactly once. Hibernate ddl-auto=validate
-- will confirm the schema matches the JPA entities on startup.
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id                       BIGSERIAL PRIMARY KEY,
    username                 VARCHAR(50)  NOT NULL UNIQUE,
    email                    VARCHAR(100) NOT NULL UNIQUE,
    password                 VARCHAR(255) NOT NULL,

    -- Stored as VARCHAR so role labels survive enum reordering.
    -- Valid values enforced at application level: ADMIN | DOCTOR | STAFF
    role                     VARCHAR(20)  NOT NULL DEFAULT 'STAFF',

    first_name               VARCHAR(50),
    last_name                VARCHAR(50),

    enabled                  BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_expired      BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_locked       BOOLEAN      NOT NULL DEFAULT TRUE,
    credentials_non_expired  BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Audit fields (populated by Spring Data JPA auditing)
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100)
);

-- Indices created declaratively in @Table on User entity are mirrored here
-- so the schema is self-contained without relying on Hibernate DDL.
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);

-- NOTE: The default admin user (Admin@123) is seeded by
-- DataInitializationService on first startup rather than SQL so that the
-- password is always hashed by BCryptPasswordEncoder at the correct cost factor.
