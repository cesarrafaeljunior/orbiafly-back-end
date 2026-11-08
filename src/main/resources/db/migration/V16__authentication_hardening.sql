ALTER TABLE app_user
  ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE password_reset_code
  ADD COLUMN failed_attempts INTEGER NOT NULL DEFAULT 0;
