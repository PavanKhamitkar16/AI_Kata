-- ============================================================
-- V3 — Create patients table
-- ============================================================

CREATE TABLE IF NOT EXISTS patients (
    patient_id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name             VARCHAR(50)  NOT NULL,
    last_name              VARCHAR(50)  NOT NULL,
    date_of_birth          DATE         NOT NULL,

    -- Stored as VARCHAR; valid values: MALE | FEMALE | OTHER
    gender                 VARCHAR(10)  NOT NULL,

    phone                  VARCHAR(20)  NOT NULL,
    email                  VARCHAR(100) UNIQUE,
    address                TEXT,
    emergency_contact      VARCHAR(100),

    -- Stored as VARCHAR; valid values: A_POS | A_NEG | B_POS | B_NEG | AB_POS | AB_NEG | O_POS | O_NEG
    blood_group            VARCHAR(10),

    medical_history        TEXT,
    allergies              TEXT,
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Audit fields (populated by Spring Data JPA auditing)
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by             VARCHAR(100),
    updated_by             VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_patients_email       ON patients (email);
CREATE INDEX IF NOT EXISTS idx_patients_name        ON patients (last_name, first_name);
CREATE INDEX IF NOT EXISTS idx_patients_blood_group ON patients (blood_group);
CREATE INDEX IF NOT EXISTS idx_patients_is_active   ON patients (is_active);
