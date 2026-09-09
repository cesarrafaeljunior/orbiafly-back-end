CREATE TABLE password_reset_code (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES app_user(id),
  code_hash VARCHAR(120) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ
);

CREATE INDEX idx_password_reset_code_user_active
  ON password_reset_code (user_id, created_at DESC)
  WHERE used_at IS NULL;
