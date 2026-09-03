-- ============================================================
-- V6 — Create appointments table
-- ============================================================

CREATE TABLE IF NOT EXISTS appointments (
    appointment_id  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id      UUID         NOT NULL REFERENCES patients(patient_id) ON DELETE RESTRICT,
    doctor_id       UUID         NOT NULL REFERENCES doctors(doctor_id)   ON DELETE RESTRICT,
    appointment_date DATE        NOT NULL,
    start_time      TIME         NOT NULL,
    end_time        TIME         NOT NULL,
    reason          VARCHAR(500) NOT NULL,

    -- Stored as VARCHAR; valid values: REQUESTED | CONFIRMED | CANCELLED | COMPLETED | NO_SHOW
    status          VARCHAR(20)  NOT NULL DEFAULT 'REQUESTED',

    notes           TEXT,

    -- Audit fields
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),

    CONSTRAINT chk_appointment_times CHECK (end_time > start_time),
    -- Prevent double-booking: same doctor cannot have two appointments starting at the same time on the same date
    CONSTRAINT uq_doctor_slot UNIQUE (doctor_id, appointment_date, start_time)
);

CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments (patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_id  ON appointments (doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointments_date       ON appointments (appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_status     ON appointments (status);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_date ON appointments (doctor_id, appointment_date);
