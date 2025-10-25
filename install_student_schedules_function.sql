-- Install get_upcoming_schedules_student function
-- This function gets upcoming schedules for students based on their enrollments

-- First ensure enrollment table exists
CREATE TABLE IF NOT EXISTS enrollment (
    enrolment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES student(student_id),
    module_id UUID NOT NULL REFERENCES modules(module_id),
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create the function
CREATE OR REPLACE FUNCTION get_upcoming_schedules_student(
    from_date DATE DEFAULT CURRENT_DATE,
    from_time TIME DEFAULT CURRENT_TIME,
    mod_id UUID DEFAULT NULL,
    stu_id UUID DEFAULT NULL,
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
        COALESCE(tp.first_name || ' ' || tp.last_name, tp.last_name, u.first_name || ' ' || u.last_name, u.first_name, u.last_name)::VARCHAR as tutor_name,
        (CASE 
            WHEN s.week_number = 0 THEN 'One-time'
            WHEN s.week_number BETWEEN 1 AND 7 THEN 'Weekly'
            WHEN s.week_number = 8 THEN 'Daily'
            ELSE 'Unknown'
        END)::VARCHAR as schedule_type
    FROM schedules s
    INNER JOIN modules m ON s.module_id = m.module_id
    INNER JOIN enrollment e ON m.module_id = e.module_id
    INNER JOIN users u ON m.tutor_id = u.id
    LEFT JOIN tutor tp ON u.id = tp.tutor_id
    WHERE 
        (stu_id IS NULL OR e.student_id = stu_id)
        AND (mod_id IS NULL OR s.module_id = mod_id)
        AND e.is_paid = TRUE  -- Only show schedules for paid enrollments
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