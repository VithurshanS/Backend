-- =====================================================
-- Database Functions and Triggers for Testing Environment
-- Transferred from need.sql
-- =====================================================

-- Insert initial data
-- INSERT INTO roles (name) VALUES ('ADMIN'), ('TUTOR'), ('STUDENT')
-- ON CONFLICT (name) DO NOTHING;

-- INSERT INTO recurrent (recurrent_type) VALUES ('Weekly'), ('Daily')
-- ON CONFLICT DO NOTHING;

-- -- Sample domains
-- INSERT INTO domain (name) VALUES
--     ('Mathematics'),
--     ('Science'),
--     ('English'),
--     ('Computer Science'),
--     ('History'),
--     ('Art')
-- ON CONFLICT DO NOTHING;

-- Function to check schedule clashes with comprehensive bidirectional logic
-- This implements "square times complex" validation - checking both directions:
-- 1. How EXISTING schedules conflict with NEW schedule
-- 2. How NEW schedule conflicts with EXISTING schedules
-- Covers all combinations of week_number patterns: 0 (one-time), 1-7 (weekly), 8 (daily)
CREATE OR REPLACE FUNCTION check_schedule_clash()
RETURNS TRIGGER AS $$
DECLARE
    clash_count INT;
    new_req_time TIME;
    new_req_date DATE;
BEGIN
    -- Use the new schedule's time and date for checking
    new_req_time := NEW.time;
    new_req_date := NEW.date;

    -- Use the same sophisticated logic as find_matching_schedule function
    WITH sched AS (
        SELECT 
            s.*,
            (s.time - interval '1 hour') AS t0,
            s.time AS t1,
            (s.time + interval '1 hour') AS t2,
            '12:00:00'::TIME as tm
        FROM schedules s
        JOIN modules m ON s.module_id = m.module_id
        WHERE m.tutor_id = (
                  SELECT m2.tutor_id
                  FROM modules m2
                  WHERE m2.module_id = NEW.module_id
              )
          -- exclude current schedule on update
          AND s.schedule_id != COALESCE(NEW.schedule_id, '00000000-0000-0000-0000-000000000000'::uuid)
    )
    SELECT COUNT(*)
    INTO clash_count
    FROM sched s
    WHERE (
		    (
		        (s.week_number = 8) 
		        AND ((NEW.week_number = 0 AND (
		            (s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2 AND s.date <= NEW.date) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (new_req_time >= s.t0 AND s.date <= new_req_date + 1) 
		                    OR (s.t2 >= new_req_time AND new_req_date >= s.date)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (new_req_time <= s.t2 AND s.date <= new_req_date - 1) 
		                    OR (new_req_time >= s.t0 AND new_req_date >= s.date)
		                ))
		            ))
		        )
		    )OR ((NEW.week_number BETWEEN 1 AND 7) AND (
		            (s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2  ) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (new_req_time >= s.t0 ) 
		                    OR (s.t2 >= new_req_time )
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (new_req_time <= s.t2) 
		                    OR (new_req_time >= s.t0)
		                ))
		            ))
		        )
		    )OR ((NEW.week_number = 8) AND (
		            (s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (new_req_time >= s.t0) 
		                    OR (s.t2 >= new_req_time)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (new_req_time <= s.t2) 
		                    OR (new_req_time >= s.t0)
		                ))
		            ))
		        )
		    ))) 
		    OR 
		    (
		        (s.week_number BETWEEN 1 AND 7) 
		        AND ((NEW.week_number = 0 AND (
		            (s.date <= NEW.date AND s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2 AND EXTRACT(ISODOW FROM NEW.date)::int = s.week_number) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (s.date-1 <= NEW.date AND new_req_time >= s.t0 AND s.week_number = EXTRACT(ISODOW FROM NEW.date+1)::int ) 
		                    OR (s.date <= NEW.date AND s.t2 >= new_req_time AND EXTRACT(ISODOW FROM NEW.date)::int = s.week_number)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (s.date +1<= NEW.date AND new_req_time <= s.t2 AND s.week_number = EXTRACT(ISODOW FROM NEW.date - 1)::int) 
		                    OR (s.date <= NEW.date AND new_req_time >= s.t0 AND EXTRACT(ISODOW FROM NEW.date)::int = s.week_number)
		                ))
		            ))
		        )
		    )OR ((NEW.week_number BETWEEN 1 AND 7) AND (
		            (s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2 AND new.week_number = s.week_number ) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (new_req_time >= s.t0 AND s.week_number = mod(new.week_number + 1, 7)) 
		                    OR (s.t2 >= new_req_time AND new.week_number = s.week_number)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (new_req_time <= s.t2 AND s.week_number = mod(new.week_number - 1, 7)) 
		                    OR (new_req_time >= s.t0 AND new.week_number = s.week_number)
		                ))
		            ))
		        )
		    )OR ((NEW.week_number = 8) AND (
		            (s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (new_req_time >= s.t0) 
		                    OR (s.t2 >= new_req_time)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (new_req_time <= s.t2) 
		                    OR (new_req_time >= s.t0)
		                ))
		            ))
		        )
		    ))) 
		    OR 
		    (
		        (s.week_number = 0) 
		        AND ((NEW.week_number = 0 AND (
		            (s.date <= NEW.date AND s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2 AND new_req_date = s.date) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (s.date -1<= NEW.date AND new_req_time >= s.t0 AND s.date = new_req_date + 1) 
		                    OR (s.date <= NEW.date AND s.t2 >= new_req_time AND new_req_date = s.date)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (s.date +1<= NEW.date AND new_req_time <= s.t2 AND s.date = new_req_date - 1) 
		                    OR (s.date <= NEW.date AND new_req_time >= s.t0 AND new_req_date = s.date)
		                ))
		            ))
		        )
		    )OR ((NEW.week_number BETWEEN 1 AND 7) AND (
		            (s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2 AND new.week_number = EXTRACT(ISODOW FROM s.date)::int ) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (new_req_time >= s.t0 AND EXTRACT(ISODOW FROM s.date-1)::int = new.week_number) 
		                    OR (s.t2 >= new_req_time AND new.week_number = EXTRACT(ISODOW FROM s.date)::int)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (new_req_time <= s.t2 AND EXTRACT(ISODOW FROM s.date + 1)::int = new.week_number) 
		                    OR (new_req_time >= s.t0 AND new.week_number = EXTRACT(ISODOW FROM s.date)::int)
		                ))
		            ))
		        )
		    )OR ((NEW.week_number = 8) AND (
		            (s.t0 < s.t2 AND s.t0 <= new_req_time AND new_req_time <= s.t2) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (new_req_time >= s.t0) 
		                    OR (s.t2 >= new_req_time)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (new_req_time <= s.t2) 
		                    OR (new_req_time >= s.t0)
		                ))
		            ))
		        )
		    )))
		);

    IF clash_count > 0 THEN
        RAISE EXCEPTION 'Schedule clash detected for tutor % - time overlap found between % and existing schedule', (
            SELECT tutor_id FROM modules WHERE module_id = NEW.module_id
        ), NEW.time;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for schedule clash checking
DROP TRIGGER IF EXISTS trg_check_schedule_clash ON schedules;
CREATE TRIGGER trg_check_schedule_clash
    BEFORE INSERT OR UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION check_schedule_clash();
-- Function to get upcoming schedules
CREATE OR REPLACE FUNCTION get_upcoming_schedules(
    from_date DATE DEFAULT CURRENT_DATE,
    from_time TIME DEFAULT CURRENT_TIME,
    mod_id UUID DEFAULT NULL,
    tut_id UUID DEFAULT NULL,
    limit_count INTEGER DEFAULT 10
) RETURNS TABLE (
    schedule_id UUID,
    module_id UUID,
    date DATE,
    active BOOLEAN,
    "time" TIME,
    duration INTEGER,
    week_number INTEGER,
    module_name VARCHAR,
    tutor_name VARCHAR,
    schedule_type VARCHAR
) AS $$
DECLARE
    from_ts TIMESTAMP;
BEGIN
    from_ts := from_date + from_time;

    RETURN QUERY
    WITH sched AS (
        SELECT 
            s.*,
            (s.time - interval '1 hour') AS t0,
            s.time AS t1,
            (s.time + interval '1 hour') AS t2,
            '12:00:00'::TIME as tm
        FROM schedules s
    )
    SELECT 
        s.schedule_id,
        s.module_id,
        CASE 
            WHEN s.week_number = 0 THEN s.date
            WHEN s.week_number BETWEEN 1 AND 7 THEN 
                CASE 
                    WHEN s.t0 < s.t2 THEN
						CASE
							WHEN EXTRACT(ISODOW FROM from_date)::int = s.week_number THEN
								CASE
									WHEN from_time<s.t2 THEN from_date
									ELSE from_date + 7
								END
							ELSE (from_date + ((s.week_number - EXTRACT(ISODOW FROM from_date)::int + 7) % 7)::int)::DATE
						END
                    ELSE 
						CASE
							WHEN s.tm>s.t1 THEN
								CASE
									WHEN from_time>s.t0 and (EXTRACT(ISODOW FROM from_date+1)::int = s.week_number) THEN from_date +1
									WHEN from_time<s.t2 and (EXTRACT(ISODOW FROM from_date)::int = s.week_number) THEN from_date
									ELSE
										CASE
											WHEN (EXTRACT(ISODOW FROM from_date)::int = s.week_number) THEN from_date + 7
											ELSE (from_date + ((s.week_number - EXTRACT(ISODOW FROM from_date)::int + 7) % 7)::int)::DATE
										END
								END
							ELSE
								CASE
									WHEN from_time<s.t2 and (EXTRACT(ISODOW FROM from_date-1)::int = s.week_number) THEN from_date-1
									WHEN from_time>s.t0 and (EXTRACT(ISODOW FROM from_date)::int = s.week_number) THEN from_date
									ELSE
										CASE
											WHEN (EXTRACT(ISODOW FROM from_date)::int = s.week_number) THEN from_date
											ELSE (from_date + ((s.week_number - EXTRACT(ISODOW FROM from_date)::int + 7) % 7)::int)::DATE
										END
								END
								
						END
                END
            WHEN s.week_number = 8 THEN 
                CASE 
                    WHEN s.t0 < s.t2 THEN
						CASE
							WHEN from_time < s.t2 THEN from_date
							ELSE from_date +1
						END
                    ELSE 
						CASE
							WHEN s.tm>s.t1 THEN
								CASE
									WHEN from_time>s.t0 THEN from_date +1
									WHEN from_time<s.t2 THEN from_date
									ELSE from_date + 1
								END
							ELSE
								CASE
									WHEN from_time<s.t2 THEN from_date-1
									WHEN from_time>s.t0 THEN from_date
									ELSE from_date
								END
								
						END
                END
            ELSE s.date
        END as calculated_date,
        (
		    (
		        (s.week_number = 8) 
		        AND (
		            (t0 < t2 AND t0 <= from_time AND from_time <= t2 AND from_date >= s.date) 
		            OR (t0 >= t2 AND (
		                (tm > t1 AND (
		                    (from_time >= t0 AND s.date <= (from_date + 1)) 
		                    OR (t2 >= from_time AND s.date <= from_date)
		                )) 
		                OR 
		                (tm < t1 AND (
		                    (from_time <= t2 AND s.date <= (from_date - 1)) 
		                    OR (from_time >= t0 AND s.date <= from_date)
		                ))
		            ))
		        )
		    ) 
		    OR 
		    (
		        (s.week_number BETWEEN 1 AND 7) 
		        AND (
		            (t0 < t2 AND t0 <= from_time AND from_time <= t2 
		             AND s.week_number = EXTRACT(ISODOW FROM from_date)::int 
		             AND from_date >= s.date
		            ) 
		            OR (t0 >= t2 AND (
		                (tm > t1 AND (
		                    (from_time >= t0 
		                     AND s.week_number = EXTRACT(ISODOW FROM (from_date + 1))::int 
		                     AND s.date <= (from_date + 1)
		                    ) 
		                    OR 
		                    (t2 >= from_time 
		                     AND s.week_number = EXTRACT(ISODOW FROM from_date)::int 
		                     AND s.date <= from_date
		                    )
		                )) 
		                OR 
		                (tm < t1 AND (
		                    (from_time <= t2 
		                     AND s.week_number = EXTRACT(ISODOW FROM (from_date - 1))::int 
		                     AND s.date <= (from_date - 1)
		                    ) 
		                    OR 
		                    (from_time >= t0 
		                     AND s.week_number = EXTRACT(ISODOW FROM from_date)::int 
		                     AND s.date <= from_date
		                    )
		                ))
		            ))
		        )
		    ) 
		    OR 
		    (
		        (s.week_number = 0) 
		        AND (
		            (t0 < t2 AND t0 <= from_time AND from_time <= t2 AND from_date = s.date) 
		            OR (t0 >= t2 AND (
		                (tm > t1 AND (
		                    (from_time >= t0 AND s.date = from_date + 1) 
		                    OR (t2 >= from_time AND from_date = s.date)
		                )) 
		                OR 
		                (tm < t1 AND (
		                    (from_time <= t2 AND s.date = from_date - 1) 
		                    OR (from_time >= t0 AND from_date = s.date)
		                ))
		            ))
		        )
		    )
		)
 			AS active,
        s."time",
        s.duration,
        s.week_number,
        m.name as module_name,
        COALESCE(tp.first_name || ' ' || tp.last_name, tp.last_name)::VARCHAR as tutor_name,
        (CASE 
            WHEN s.week_number = 0 THEN 'One-time'
            WHEN s.week_number BETWEEN 1 AND 7 THEN 'Weekly'
            WHEN s.week_number = 8 THEN 'Daily'
            ELSE 'Unknown'
        END)::VARCHAR as schedule_type
    FROM sched s
    LEFT JOIN modules m ON s.module_id = m.module_id
    LEFT JOIN tutor tp ON m.tutor_id = tp.tutor_id
    LEFT JOIN users u ON m.tutor_id = u.id
    WHERE 
        (mod_id IS NULL OR s.module_id=mod_id)
        AND (tut_id IS NULL OR u.id=tut_id)
        AND (
            ((s.week_number = 8))
            OR ((s.week_number BETWEEN 1 AND 7))
            OR ((s.week_number = 0) AND (
                (s.t0 < s.t2 AND ((s.date>from_date) OR (s.date = from_date and from_time <= s.t2)))
                OR ((s.t0>s.t2) AND ((s.date>from_date)OR(s.tm<s.t1 and from_time<s.t2 and s.date=(from_date -1))) ))
            )
        )
    ORDER BY 
        calculated_date ASC, 
        s."time" ASC,
        s.schedule_id
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;

-- Student version of get_upcoming_schedules
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
    active BOOLEAN,
    "time" TIME,
    duration INTEGER,
    week_number INTEGER,
    module_name VARCHAR,
    tutor_name VARCHAR,
    schedule_type VARCHAR
) AS $$
DECLARE
    from_ts TIMESTAMP;
BEGIN
    from_ts := from_date + from_time;

    RETURN QUERY
    WITH sched AS (
        SELECT 
            s.*,
            (s.time - interval '1 hour') AS t0,
            s.time AS t1,
            (s.time + interval '1 hour') AS t2,
            '12:00:00'::TIME as tm
        FROM schedules s
        INNER JOIN modules m ON s.module_id = m.module_id
        INNER JOIN enrollment e ON m.module_id = e.module_id
        WHERE 
            (stu_id IS NULL OR e.student_id = stu_id)
            AND e.is_paid = TRUE  -- Only show schedules for paid enrollments
    )
    SELECT 
        s.schedule_id,
        s.module_id,
        CASE 
            WHEN s.week_number = 0 THEN s.date
            WHEN s.week_number BETWEEN 1 AND 7 THEN 
                CASE 
                    WHEN s.t0 < s.t2 THEN
						CASE
							WHEN EXTRACT(ISODOW FROM from_date)::int = s.week_number THEN
								CASE
									WHEN from_time<s.t2 THEN from_date
									ELSE from_date + 7
								END
							ELSE (from_date + ((s.week_number - EXTRACT(ISODOW FROM from_date)::int + 7) % 7)::int)::DATE
						END
                    ELSE 
						CASE
							WHEN s.tm>s.t1 THEN
								CASE
									WHEN from_time>s.t0 and (EXTRACT(ISODOW FROM from_date+1)::int = s.week_number) THEN from_date +1
									WHEN from_time<s.t2 and (EXTRACT(ISODOW FROM from_date)::int = s.week_number) THEN from_date
									ELSE
										CASE
											WHEN (EXTRACT(ISODOW FROM from_date)::int = s.week_number) THEN from_date + 7
											ELSE (from_date + ((s.week_number - EXTRACT(ISODOW FROM from_date)::int + 7) % 7)::int)::DATE
										END
								END
							ELSE
								CASE
									WHEN from_time<s.t2 and (EXTRACT(ISODOW FROM from_date-1)::int = s.week_number) THEN from_date-1
									WHEN from_time>s.t0 and (EXTRACT(ISODOW FROM from_date)::int = s.week_number) THEN from_date
									ELSE
										CASE
											WHEN (EXTRACT(ISODOW FROM from_date)::int = s.week_number) THEN from_date
											ELSE (from_date + ((s.week_number - EXTRACT(ISODOW FROM from_date)::int + 7) % 7)::int)::DATE
										END
								END
								
						END
                END
            WHEN s.week_number = 8 THEN 
                CASE 
                    WHEN s.t0 < s.t2 THEN
						CASE
							WHEN from_time < s.t2 THEN from_date
							ELSE from_date +1
						END
                    ELSE 
						CASE
							WHEN s.tm>s.t1 THEN
								CASE
									WHEN from_time>s.t0 THEN from_date +1
									WHEN from_time<s.t2 THEN from_date
									ELSE from_date + 1
								END
							ELSE
								CASE
									WHEN from_time<s.t2 THEN from_date-1
									WHEN from_time>s.t0 THEN from_date
									ELSE from_date
								END
								
						END
                END
            ELSE s.date
        END as calculated_date,
        (
		    (
		        (s.week_number = 8) 
		        AND (
		            (t0 < t2 AND t0 <= from_time AND from_time <= t2 AND from_date >= s.date) 
		            OR (t0 >= t2 AND (
		                (tm > t1 AND (
		                    (from_time >= t0 AND s.date <= (from_date + 1)) 
		                    OR (t2 >= from_time AND s.date <= from_date)
		                )) 
		                OR 
		                (tm < t1 AND (
		                    (from_time <= t2 AND s.date <= (from_date - 1)) 
		                    OR (from_time >= t0 AND s.date <= from_date)
		                ))
		            ))
		        )
		    ) 
		    OR 
		    (
		        (s.week_number BETWEEN 1 AND 7) 
		        AND (
		            (t0 < t2 AND t0 <= from_time AND from_time <= t2 
		             AND s.week_number = EXTRACT(ISODOW FROM from_date)::int 
		             AND from_date >= s.date
		            ) 
		            OR (t0 >= t2 AND (
		                (tm > t1 AND (
		                    (from_time >= t0 
		                     AND s.week_number = EXTRACT(ISODOW FROM (from_date + 1))::int 
		                     AND s.date <= (from_date + 1)
		                    ) 
		                    OR 
		                    (t2 >= from_time 
		                     AND s.week_number = EXTRACT(ISODOW FROM from_date)::int 
		                     AND s.date <= from_date
		                    )
		                )) 
		                OR 
		                (tm < t1 AND (
		                    (from_time <= t2 
		                     AND s.week_number = EXTRACT(ISODOW FROM (from_date - 1))::int 
		                     AND s.date <= (from_date - 1)
		                    ) 
		                    OR 
		                    (from_time >= t0 
		                     AND s.week_number = EXTRACT(ISODOW FROM from_date)::int 
		                     AND s.date <= from_date
		                    )
		                ))
		            ))
		        )
		    ) 
		    OR 
		    (
		        (s.week_number = 0) 
		        AND (
		            (t0 < t2 AND t0 <= from_time AND from_time <= t2 AND from_date = s.date) 
		            OR (t0 >= t2 AND (
		                (tm > t1 AND (
		                    (from_time >= t0 AND s.date = from_date + 1) 
		                    OR (t2 >= from_time AND from_date = s.date)
		                )) 
		                OR 
		                (tm < t1 AND (
		                    (from_time <= t2 AND s.date = from_date - 1) 
		                    OR (from_time >= t0 AND from_date = s.date)
		                ))
		            ))
		        )
		    )
		)
 			AS active,
        s."time",
        s.duration,
        s.week_number,
        m.name as module_name,
        COALESCE(tp.first_name || ' ' || tp.last_name, tp.last_name)::VARCHAR as tutor_name,
        (CASE 
            WHEN s.week_number = 0 THEN 'One-time'
            WHEN s.week_number BETWEEN 1 AND 7 THEN 'Weekly'
            WHEN s.week_number = 8 THEN 'Daily'
            ELSE 'Unknown'
        END)::VARCHAR as schedule_type
    FROM sched s
    LEFT JOIN modules m ON s.module_id = m.module_id
    LEFT JOIN tutor tp ON m.tutor_id = tp.tutor_id
    LEFT JOIN users u ON m.tutor_id = u.id
    WHERE 
        (mod_id IS NULL OR s.module_id=mod_id)
        AND (
            ((s.week_number = 8))
            OR ((s.week_number BETWEEN 1 AND 7))
            OR ((s.week_number = 0) AND (
                (s.t0 < s.t2 AND ((s.date>from_date) OR (s.date = from_date and from_time <= s.t0)))
                OR ((s.t0>s.t2) AND ((s.date>from_date)OR(s.tm<s.t1 and from_time<s.t2 and s.date=(from_date -1))) ))
            )
        )
    ORDER BY 
        calculated_date ASC, 
        s."time" ASC,
        s.schedule_id
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;

-- Function to find matching schedule
CREATE OR REPLACE FUNCTION find_matching_schedule(
    req_date DATE,
    req_time TIME,
    mod_id UUID
) RETURNS UUID AS $$
DECLARE
    matched_schedule UUID;
    req_ts TIMESTAMP;
BEGIN
    -- build full timestamp for request
    req_ts := req_date + req_time;

    -- Use the same active logic as get_upcoming_schedules
    WITH sched AS (
        SELECT 
            s.*,
            (s.time - interval '1 hour') AS t0,
            s.time AS t1,
            (s.time + interval '1 hour') AS t2,
            '12:00:00'::TIME as tm
        FROM schedules s
        WHERE s.module_id = mod_id
    )
    SELECT s.schedule_id
    INTO matched_schedule
    FROM sched s
    WHERE (
		    (
		        (s.week_number = 8) 
		        AND (
		            (s.t0 < s.t2 AND s.t0 <= req_time AND req_time <= s.t2 AND req_date >= s.date) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (req_time >= s.t0 AND s.date <= (req_date + 1)) 
		                    OR (s.t2 >= req_time AND s.date <= req_date)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (req_time <= s.t2 AND s.date <= (req_date - 1)) 
		                    OR (req_time >= s.t0 AND s.date <= req_date)
		                ))
		            ))
		        )
		    ) 
		    OR 
		    (
		        (s.week_number BETWEEN 1 AND 7) 
		        AND (
		            (s.t0 < s.t2 AND s.t0 <= req_time AND req_time <= s.t2 
		             AND s.week_number = EXTRACT(ISODOW FROM req_date)::int 
		             AND req_date >= s.date
		            ) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (req_time >= s.t0 
		                     AND s.week_number = EXTRACT(ISODOW FROM (req_date + 1))::int 
		                     AND s.date <= (req_date + 1)
		                    ) 
		                    OR 
		                    (s.t2 >= req_time 
		                     AND s.week_number = EXTRACT(ISODOW FROM req_date)::int 
		                     AND s.date <= req_date
		                    )
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (req_time <= s.t2 
		                     AND s.week_number = EXTRACT(ISODOW FROM (req_date - 1))::int 
		                     AND s.date <= (req_date - 1)
		                    ) 
		                    OR 
		                    (req_time >= s.t0 
		                     AND s.week_number = EXTRACT(ISODOW FROM req_date)::int 
		                     AND s.date <= req_date
		                    )
		                ))
		            ))
		        )
		    ) 
		    OR 
		    (
		        (s.week_number = 0) 
		        AND (
		            (s.t0 < s.t2 AND s.t0 <= req_time AND req_time <= s.t2 AND req_date = s.date) 
		            OR (s.t0 >= s.t2 AND (
		                (s.tm > s.t1 AND (
		                    (req_time >= s.t0 AND s.date = req_date + 1) 
		                    OR (s.t2 >= req_time AND req_date = s.date)
		                )) 
		                OR 
		                (s.tm < s.t1 AND (
		                    (req_time <= s.t2 AND s.date = req_date - 1) 
		                    OR (req_time >= s.t0 AND req_date = s.date)
		                ))
		            ))
		        )
		    )
		)
    ORDER BY s.date, s.time
    LIMIT 1;

    IF matched_schedule IS NULL THEN
        RAISE EXCEPTION 'No matching schedule found for module % on % at %', mod_id, req_date, req_time;
    END IF;

    RETURN matched_schedule;
END;
$$ LANGUAGE plpgsql;

-- Function to get all emails for a specific module
CREATE OR REPLACE FUNCTION get_module_emails(mod_id UUID)
RETURNS TABLE (
    email VARCHAR,
    user_type VARCHAR,
    user_id UUID,
    user_name VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    -- Get tutor email
    SELECT 
        u.email,
        'TUTOR'::VARCHAR as user_type,
        u.id as user_id,
        u.name as user_name
    FROM modules m
    JOIN users u ON m.tutor_id = u.id
    WHERE m.module_id = mod_id
    
    UNION ALL
    
    -- Get enrolled students' emails (where is_paid = true means enrollment is paid/active)
    SELECT 
        u.email,
        'STUDENT'::VARCHAR as user_type,
        u.id as user_id,
        u.name as user_name
    FROM enrollment e
    JOIN users u ON e.student_id = u.id
    WHERE e.module_id = mod_id 
    AND e.is_paid = true;
END;
$$ LANGUAGE plpgsql;

-- Function to update module average rating
CREATE OR REPLACE FUNCTION update_module_average_rating()
RETURNS TRIGGER AS $$
DECLARE
    target_module_id UUID;
    avg_rating DECIMAL(3,1);
    rating_count INTEGER;
BEGIN
    -- Determine which module_id to update based on the operation
    IF TG_OP = 'DELETE' THEN
        target_module_id := OLD.module_id;
    ELSE
        target_module_id := NEW.module_id;
    END IF;

    -- For UPDATE operations, we might need to update both old and new modules
    -- if the module_id changed (though this is unlikely in practice)
    IF TG_OP = 'UPDATE' AND OLD.module_id != NEW.module_id THEN
        -- Update the old module first
        SELECT 
            COALESCE(AVG(rating), 0.0),
            COUNT(*)
        INTO avg_rating, rating_count
        FROM rating 
        WHERE module_id = OLD.module_id 
        AND rating IS NOT NULL;

        UPDATE modules 
        SET average_ratings = ROUND(avg_rating, 1),
            updated_at = CURRENT_TIMESTAMP
        WHERE module_id = OLD.module_id;

        -- Log the update for the old module
        RAISE NOTICE 'Updated average rating for module % to % (based on % ratings)', 
            OLD.module_id, ROUND(avg_rating, 1), rating_count;
    END IF;

    -- Calculate new average rating for the target module
    SELECT 
        COALESCE(AVG(rating), 0.0),
        COUNT(*)
    INTO avg_rating, rating_count
    FROM rating 
    WHERE module_id = target_module_id 
    AND rating IS NOT NULL;

    -- Update the modules table with the new average rating
    UPDATE modules 
    SET average_ratings = ROUND(avg_rating, 1),
        updated_at = CURRENT_TIMESTAMP
    WHERE module_id = target_module_id;

    -- Log the update
    RAISE NOTICE 'Updated average rating for module % to % (based on % ratings)', 
        target_module_id, ROUND(avg_rating, 1), rating_count;

    -- Return the appropriate record based on operation
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for updating module average rating
DROP TRIGGER IF EXISTS trg_update_module_average_rating ON rating;
CREATE TRIGGER trg_update_module_average_rating
    AFTER INSERT OR UPDATE OR DELETE ON rating
    FOR EACH ROW 
    EXECUTE FUNCTION update_module_average_rating();