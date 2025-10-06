-- Migration for schedule notifications table
CREATE TABLE IF NOT EXISTS schedule_notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id UUID NOT NULL REFERENCES schedules(schedule_id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
    notification_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster queries
CREATE INDEX IF NOT EXISTS idx_schedule_notifications_schedule_id ON schedule_notifications(schedule_id);
CREATE INDEX IF NOT EXISTS idx_schedule_notifications_status ON schedule_notifications(status);
CREATE INDEX IF NOT EXISTS idx_schedule_notifications_type ON schedule_notifications(notification_type);

-- Unique constraint to prevent duplicate notifications for the same schedule and type
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_schedule_notification 
ON schedule_notifications(schedule_id, notification_type) 
WHERE status IN ('PENDING', 'SENT');