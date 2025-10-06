-- Create table to track fired notifications
CREATE TABLE IF NOT EXISTS notification_tracking (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    notification_fired_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Create unique constraint to prevent duplicate notifications
    UNIQUE(schedule_id, scheduled_date, scheduled_time)
);

-- Create index for better performance on cleanup queries
CREATE INDEX IF NOT EXISTS idx_notification_tracking_date 
ON notification_tracking (notification_fired_at);

-- Create index for lookup performance
CREATE INDEX IF NOT EXISTS idx_notification_tracking_schedule 
ON notification_tracking (schedule_id, scheduled_date, scheduled_time);

-- Add comment for documentation
COMMENT ON TABLE notification_tracking IS 'Tracks fired email notifications to prevent duplicates';
COMMENT ON COLUMN notification_tracking.schedule_id IS 'Reference to the schedule that triggered the notification';
COMMENT ON COLUMN notification_tracking.scheduled_date IS 'Date of the scheduled class';
COMMENT ON COLUMN notification_tracking.scheduled_time IS 'Time of the scheduled class';
COMMENT ON COLUMN notification_tracking.notification_fired_at IS 'When the notification was fired';