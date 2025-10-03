-- Migration to consolidate first_name and last_name back into single name field in users table only

-- Add name column back to users table
ALTER TABLE users ADD COLUMN name VARCHAR(255);

-- Update existing data (combine first_name and last_name into name)
UPDATE users
SET name = TRIM(COALESCE(first_name, '') || ' ' || COALESCE(last_name, ''))
WHERE first_name IS NOT NULL OR last_name IS NOT NULL;

-- Drop the first_name and last_name columns from users table only
ALTER TABLE users DROP COLUMN first_name;
ALTER TABLE users DROP COLUMN last_name;

-- Keep first_name and last_name in student and tutor tables (no changes needed)