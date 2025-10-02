-- =============================================
-- Complete PostgreSQL Schema for Tutorverse
-- Generated from JPA Entities in Model folder
-- =============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- TABLE CREATION
-- =============================================

-- Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    email VARCHAR(255) UNIQUE NOT NULL,
    providerid VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    name VARCHAR(255),
    is_email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student Table
CREATE TABLE IF NOT EXISTS student (
    student_id UUID PRIMARY KEY REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    birthday DATE,
    image_url VARCHAR(255),
    last_accessed DATE,
    is_active BOOLEAN,
    phone_number VARCHAR(255),
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tutor Table
CREATE TABLE IF NOT EXISTS tutor (
    tutor_id UUID PRIMARY KEY REFERENCES users(id),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_no VARCHAR(255) UNIQUE NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    dob DATE,
    last_accessed DATE,
    image TEXT,
    portfolio TEXT,
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Domain Table
CREATE TABLE IF NOT EXISTS domain (
    domain_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Recurrent Table
CREATE TABLE IF NOT EXISTS recurrent (
    recurrent_id SERIAL PRIMARY KEY,
    recurrent_type VARCHAR(50) NOT NULL
);

-- Modules Table
CREATE TABLE IF NOT EXISTS modules (
    module_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tutor_id UUID NOT NULL REFERENCES tutor(tutor_id),
    name VARCHAR(255) NOT NULL,
    domain INTEGER REFERENCES domain(domain_id),
    average_ratings DECIMAL(3,1) DEFAULT 0.0,
    fee DECIMAL(10,2) NOT NULL,
    duration INTERVAL,
    status VARCHAR(20) CHECK (status IN ('Draft', 'Active', 'Archived')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_fee_positive CHECK (fee > 0),
    CONSTRAINT check_ratings_range CHECK (average_ratings >= 0 AND average_ratings <= 5)
);

-- Schedules Table
CREATE TABLE IF NOT EXISTS schedules (
    schedule_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id UUID NOT NULL REFERENCES modules(module_id),
    date DATE NOT NULL,
    time TIME NOT NULL,
    duration INTEGER NOT NULL, -- in minutes
    week_number INTEGER,       -- 0 = specific date, 1-7 = weekly, 8 = daily
    recurrent_id INTEGER REFERENCES recurrent(recurrent_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_schedule_time_valid CHECK (date >= CURRENT_DATE OR date = CURRENT_DATE)
);

-- =============================================
-- AUDIT TRIGGERS (auto update updated_at)
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to all tables with updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_student_updated_at BEFORE UPDATE ON student
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tutor_updated_at BEFORE UPDATE ON tutor
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_modules_updated_at BEFORE UPDATE ON modules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_schedules_updated_at BEFORE UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- BUSINESS LOGIC TRIGGERS
-- =============================================

-- Schedule Clash Prevention Trigger
CREATE OR REPLACE FUNCTION check_schedule_clash()
RETURNS TRIGGER AS $$
DECLARE
    clash_count INT;
BEGIN
    SELECT COUNT(*)
    INTO clash_count
    FROM schedules s
    JOIN modules m ON s.module_id = m.module_id
    WHERE m.tutor_id = (
              SELECT m2.tutor_id
              FROM modules m2
              WHERE m2.module_id = NEW.module_id
          )
      AND (
            -- specific date
            (NEW.week_number = 0 AND s.date = NEW.date)
            -- weekly
            OR (NEW.week_number BETWEEN 1 AND 7 AND s.week_number = NEW.week_number)
            -- daily
            OR (NEW.week_number = 8)
          )
      -- time overlap check
      AND (
            NEW.time < (s.time + (s.duration || ' minutes')::interval)
            AND s.time < (NEW.time + (NEW.duration || ' minutes')::interval)
          )
      -- exclude current schedule on update
      AND s.schedule_id != COALESCE(NEW.schedule_id, '00000000-0000-0000-0000-000000000000'::uuid);

    IF clash_count > 0 THEN
        RAISE EXCEPTION 'Schedule clash detected for tutor %', (
            SELECT tutor_id FROM modules WHERE module_id = NEW.module_id
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_check_schedule_clash ON schedules;
CREATE TRIGGER trg_check_schedule_clash
    BEFORE INSERT OR UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION check_schedule_clash();

-- =============================================
-- INDEXES FOR PERFORMANCE
-- =============================================
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_tutor_phone_no ON tutor(phone_no);
CREATE INDEX IF NOT EXISTS idx_modules_tutor_id ON modules(tutor_id);
CREATE INDEX IF NOT EXISTS idx_modules_domain ON modules(domain);
CREATE INDEX IF NOT EXISTS idx_schedules_module_id ON schedules(module_id);
CREATE INDEX IF NOT EXISTS idx_schedules_date_time ON schedules(date, time);

-- =============================================
-- DEFAULT SEED DATA
-- =============================================
INSERT INTO roles (name) VALUES ('ADMIN'), ('TUTOR'), ('STUDENT')
ON CONFLICT (name) DO NOTHING;

INSERT INTO recurrent (recurrent_type) VALUES ('Weekly'), ('Daily')
ON CONFLICT DO NOTHING;

-- Sample domains
INSERT INTO domain (name) VALUES
    ('Mathematics'),
    ('Science'),
    ('English'),
    ('Computer Science'),
    ('History'),
    ('Art')
ON CONFLICT DO NOTHING;

-- =============================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================
COMMENT ON TABLE roles IS 'User role definitions (ADMIN, TUTOR, STUDENT)';
COMMENT ON TABLE users IS 'Base user information for all user types';
COMMENT ON TABLE student IS 'Student-specific profile information';
COMMENT ON TABLE tutor IS 'Tutor-specific profile information';
COMMENT ON TABLE domain IS 'Subject domains for tutoring modules';
COMMENT ON TABLE modules IS 'Tutoring modules offered by tutors';
COMMENT ON TABLE recurrent IS 'Schedule recurrence types';
COMMENT ON TABLE schedules IS 'Module scheduling information';

COMMENT ON COLUMN schedules.week_number IS '0=specific date, 1-7=weekly (Mon-Sun), 8=daily';
COMMENT ON COLUMN schedules.duration IS 'Duration in minutes';
COMMENT ON COLUMN modules.duration IS 'Total module duration as PostgreSQL interval';
COMMENT ON COLUMN modules.average_ratings IS 'Average rating from 0.0 to 5.0';


CREATE OR REPLACE FUNCTION get_upcoming_schedules(
    from_date DATE DEFAULT CURRENT_DATE,
    from_time TIME DEFAULT CURRENT_TIME,
    mod_id UUID DEFAULT NULL,
    limit_count INTEGER DEFAULT 10
) RETURNS TABLE (
    schedule_id UUID,
    module_id UUID,
    date DATE,
    "time" TIME,
    duration INTEGER,
    week_number INTEGER,
    module_name VARCHAR,
    tutor_name VARCHAR,
    schedule_type VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.schedule_id,
        s.module_id,
        CASE 
            WHEN s.week_number = 0 THEN s.date
            WHEN s.week_number BETWEEN 1 AND 7 THEN 
                CASE 
                    WHEN s.date > from_date THEN s.date
                    ELSE (from_date + ((s.week_number - EXTRACT(ISODOW FROM from_date)::int + 7) % 7)::int)::DATE
                END
            WHEN s.week_number = 8 THEN 
                CASE 
                    WHEN from_time < s."time" AND s.date <= from_date THEN from_date
                    ELSE from_date + 1
                END
            ELSE s.date
        END as calculated_date,
        s."time",
        s.duration,
        s.week_number,
        m.name as module_name,
        COALESCE(tp.first_name || ' ' || tp.last_name, tp.last_name, u.name)::VARCHAR as tutor_name,
		(CASE 
		    WHEN s.week_number = 0 THEN 'One-time'
		    WHEN s.week_number BETWEEN 1 AND 7 THEN 'Weekly'
		    WHEN s.week_number = 8 THEN 'Daily'
		    ELSE 'Unknown'
		END)::VARCHAR as schedule_type
    FROM schedules s
    INNER JOIN modules m ON s.module_id = m.module_id
    INNER JOIN users u ON m.tutor_id = u.id
    LEFT JOIN tutor tp ON u.id = tp.tutor_id
    WHERE 
        (mod_id IS NULL OR s.module_id = mod_id)
        AND (
            (s.week_number = 0 
             AND (s.date > from_date 
                  OR (s.date = from_date AND s."time" > from_time)))
            OR (s.week_number BETWEEN 1 AND 7
                AND (s.date > from_date 
                     OR (s.date <= from_date 
                         AND (EXTRACT(ISODOW FROM from_date)::int < s.week_number
                              OR (EXTRACT(ISODOW FROM from_date)::int = s.week_number 
                                  AND from_time < s."time")))))
            OR (s.week_number = 8
                AND (s.date > from_date 
                     OR (s.date <= from_date AND s."time" > from_time)))
        )
    ORDER BY 
        calculated_date ASC, 
        s."time" ASC,
        s.schedule_id
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;