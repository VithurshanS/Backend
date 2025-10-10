-- Create event_publication table for Spring Modulith
CREATE TABLE IF NOT EXISTS event_publication (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    completion_date TIMESTAMP
);

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_event_publication_completion_date ON event_publication(completion_date);
CREATE INDEX IF NOT EXISTS idx_event_publication_publication_date ON event_publication(publication_date);