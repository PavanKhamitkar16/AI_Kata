-- ============================================================
-- V4 — Create doctor_schedules table
-- ============================================================

CREATE TABLE IF NOT EXISTS doctor_schedules (
    schedule_id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id              UUID         NOT NULL REFERENCES doctors(doctor_id) ON DELETE CASCADE,

    -- Stored as VARCHAR; valid values: MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY | SATURDAY | SUNDAY
    day_of_week            VARCHAR(10)  NOT NULL,

    start_time             TIME         NOT NULL,
    end_time               TIME         NOT NULL,
    slot_duration_minutes  INTEGER      NOT NULL DEFAULT 30,

    -- Audit fields
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by             VARCHAR(100),
    updated_by             VARCHAR(100),

    CONSTRAINT uq_doctor_day UNIQUE (doctor_id, day_of_week),
    CONSTRAINT chk_schedule_times CHECK (end_time > start_time),
    CONSTRAINT chk_slot_duration  CHECK (slot_duration_minutes > 0)
);

CREATE INDEX IF NOT EXISTS idx_doctor_schedules_doctor_id ON doctor_schedules (doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_schedules_day       ON doctor_schedules (day_of_week);
