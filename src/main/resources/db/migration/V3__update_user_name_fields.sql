-- Migration to split name field into first_name and last_name
-- Add new columns
ALTER TABLE users ADD COLUMN first_name VARCHAR(255);
ALTER TABLE users ADD COLUMN last_name VARCHAR(255);

-- Update existing data (split name into first and last name where possible)
UPDATE users
SET first_name = CASE
    WHEN position(' ' in name) > 0 THEN split_part(name, ' ', 1)
    ELSE name
END,
last_name = CASE
    WHEN position(' ' in name) > 0 THEN substring(name from position(' ' in name) + 1)
    ELSE ''
END
WHERE name IS NOT NULL;

-- Drop the old name column
ALTER TABLE users DROP COLUMN name;

-- Also update the student table to use first_name and last_name instead of name
ALTER TABLE student ADD COLUMN first_name VARCHAR(255);
ALTER TABLE student ADD COLUMN last_name VARCHAR(255);

-- Update student table data
UPDATE student
SET first_name = CASE
    WHEN position(' ' in name) > 0 THEN split_part(name, ' ', 1)
    ELSE name
END,
last_name = CASE
    WHEN position(' ' in name) > 0 THEN substring(name from position(' ' in name) + 1)
    ELSE ''
END
WHERE name IS NOT NULL;

-- Drop the old name column from student table
ALTER TABLE student DROP COLUMN name;
