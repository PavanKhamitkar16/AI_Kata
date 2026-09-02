-- ============================================================
-- V2 — Create doctors table
-- ============================================================

CREATE TABLE IF NOT EXISTS doctors (
    doctor_id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name                   VARCHAR(100) NOT NULL,
    email                  VARCHAR(100) NOT NULL UNIQUE,
    phone                  VARCHAR(20)  NOT NULL,
    specialization         VARCHAR(100) NOT NULL,
    qualification          VARCHAR(200) NOT NULL,
    experience_years       INTEGER,
    department             VARCHAR(100) NOT NULL,

    -- Stored as VARCHAR; valid values: AVAILABLE | UNAVAILABLE | ON_LEAVE
    availability_status    VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',

    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Audit fields (populated by Spring Data JPA auditing)
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by             VARCHAR(100),
    updated_by             VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_doctors_email           ON doctors (email);
CREATE INDEX IF NOT EXISTS idx_doctors_specialization  ON doctors (specialization);
CREATE INDEX IF NOT EXISTS idx_doctors_department      ON doctors (department);
CREATE INDEX IF NOT EXISTS idx_doctors_status          ON doctors (availability_status);
