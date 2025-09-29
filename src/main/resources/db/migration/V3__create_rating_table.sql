-- Flyway migration to create rating table (shared PK with enrollment)
-- Only needed if table does not already exist. Safe IF NOT EXISTS guards used.

-- Ensure extension (harmless if already there)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create rating table with shared primary key referencing enrollment
CREATE TABLE IF NOT EXISTS rating (
    enrolment_id UUID PRIMARY KEY REFERENCES enrollment(enrolment_id) ON DELETE CASCADE,
    rating DECIMAL(2,1),
    feedback TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_name VARCHAR(255),
    module_id UUID NOT NULL,
    CONSTRAINT chk_rating_range CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5))
);

-- Index to optimize module lookups
CREATE INDEX IF NOT EXISTS idx_rating_module_id ON rating(module_id);
