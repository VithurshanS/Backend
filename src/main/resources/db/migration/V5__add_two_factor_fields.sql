-- Add TOTP/2FA fields to users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS totp_secret VARCHAR(255),
    ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;
