-- =============================================
-- Flyway Migration: Initial Schema for Tutorverse
-- Version: V1__init_schema.sql
-- =============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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

-- Recurrent Table
CREATE TABLE IF NOT EXISTS recurrent (
                                         recurrent_id SERIAL PRIMARY KEY,
                                         recurrent_type VARCHAR(50) NOT NULL
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
-- Audit Triggers (auto update updated_at)
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers
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
-- Schedule Clash Trigger
-- =============================================
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
    
    (NEW.week_number = 0 AND s.date = NEW.date)
        OR (NEW.week_number BETWEEN 1 AND 7 AND s.week_number = NEW.week_number)
        OR (NEW.week_number = 8)
    )

  AND (
    NEW.time < (s.time + (s.duration || ' minutes')::interval)
        AND s.time < (NEW.time + (NEW.duration || ' minutes')::interval)
    )

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


INSERT INTO roles (name) VALUES ('ADMIN'), ('TUTOR'), ('STUDENT')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO recurrent (recurrent_type) VALUES ('Weekly'), ('Daily')
    ON CONFLICT DO NOTHING;


CREATE OR REPLACE FUNCTION find_matching_schedule(
    req_date DATE,
    req_time TIME,
    mod_id UUID
) RETURNS UUID AS $$
DECLARE
    matched_schedule UUID;
BEGIN
    SELECT s.schedule_id
    INTO matched_schedule
    FROM schedules s
    WHERE s.module_id = mod_id
      AND (
          
            (s.week_number = 0 AND s.date = req_date
                 AND req_time BETWEEN (s.time - interval '1 hour')
                                   AND (s.time + (s.duration || ' minutes')::interval + interval '1 hour'))

       
            OR (s.week_number BETWEEN 1 AND 7
                 AND EXTRACT(ISODOW FROM req_date)::int = s.week_number
                 AND req_time BETWEEN (s.time - interval '1 hour')
                                   AND (s.time + (s.duration || ' minutes')::interval + interval '1 hour'))

            
            OR (s.week_number = 8
                 AND req_time BETWEEN (s.time - interval '1 hour')
                                   AND (s.time + (s.duration || ' minutes')::interval + interval '1 hour'))
          )
    LIMIT 1;

    IF matched_schedule IS NULL THEN
        RAISE EXCEPTION 'No matching schedule found for module % on % at %', mod_id, req_date, req_time;
    END IF;

    RETURN matched_schedule;
END;
$$ LANGUAGE plpgsql;
