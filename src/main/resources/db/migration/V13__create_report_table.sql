-- Create 'report' table with all required columns
CREATE TABLE report (
    report_id UUID PRIMARY KEY NOT NULL,
    module_id UUID NOT NULL,
    reason TEXT NOT NULL,
    reported_by UUID NOT NULL,
    report_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);