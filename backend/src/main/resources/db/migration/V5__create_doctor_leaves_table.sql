-- ============================================================
-- V5 — Create doctor_leaves table
-- ============================================================

CREATE TABLE IF NOT EXISTS doctor_leaves (
    leave_id     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id    UUID         NOT NULL REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    start_date   DATE         NOT NULL,
    end_date     DATE         NOT NULL,
    reason       VARCHAR(500),

    -- Audit fields
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),

    CONSTRAINT chk_leave_dates CHECK (end_date >= start_date)
);

CREATE INDEX IF NOT EXISTS idx_doctor_leaves_doctor_id   ON doctor_leaves (doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_leaves_date_range  ON doctor_leaves (doctor_id, start_date, end_date);
