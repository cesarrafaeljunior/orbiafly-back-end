CREATE TABLE app_user (
  id UUID PRIMARY KEY,
  email VARCHAR(180) NOT NULL UNIQUE,
  username VARCHAR(80) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_app_user_email_not_blank CHECK (btrim(email) <> ''),
  CONSTRAINT ck_app_user_username_not_blank CHECK (btrim(username) <> ''),
  CONSTRAINT ck_app_user_password_hash_not_blank CHECK (btrim(password_hash) <> '')
);

CREATE TABLE app_user_role (
  user_id UUID NOT NULL REFERENCES app_user (id),
  role VARCHAR(30) NOT NULL,
  CONSTRAINT pk_app_user_role PRIMARY KEY (user_id, role),
  CONSTRAINT ck_app_user_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

INSERT INTO app_user (id, email, username, password_hash, active)
VALUES (
  '00000000-0000-0000-0000-000000000001',
  'admin@admin.com.br',
  'admin',
  '$2a$10$1Ew0gT6vXU4XJ/dx89bXUO8nI/SNfndH.61gQMVGrJw79oZOVknFC',
  TRUE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO app_user_role (user_id, role)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'ROLE_USER'),
  ('00000000-0000-0000-0000-000000000001', 'ROLE_ADMIN')
ON CONFLICT DO NOTHING;

ALTER TABLE financial_account ADD COLUMN owner_user_id UUID;
ALTER TABLE financial_transaction ADD COLUMN owner_user_id UUID;
ALTER TABLE fixed_expense ADD COLUMN owner_user_id UUID;
ALTER TABLE fixed_expense_occurrence ADD COLUMN owner_user_id UUID;
ALTER TABLE credit_card ADD COLUMN owner_user_id UUID;
ALTER TABLE credit_card_purchase ADD COLUMN owner_user_id UUID;
ALTER TABLE credit_card_installment ADD COLUMN owner_user_id UUID;
ALTER TABLE loan ADD COLUMN owner_user_id UUID;
ALTER TABLE loan_installment ADD COLUMN owner_user_id UUID;
ALTER TABLE loan_entry ADD COLUMN owner_user_id UUID;

UPDATE financial_account SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE financial_transaction SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE fixed_expense SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE fixed_expense_occurrence SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE credit_card SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE credit_card_purchase SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE credit_card_installment SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE loan SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE loan_installment SET owner_user_id = '00000000-0000-0000-0000-000000000001';
UPDATE loan_entry SET owner_user_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE financial_account
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_financial_account_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE financial_transaction
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_financial_transaction_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE fixed_expense
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_fixed_expense_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE fixed_expense_occurrence
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_fixed_expense_occurrence_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE credit_card
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_credit_card_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE credit_card_purchase
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_credit_card_purchase_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE credit_card_installment
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_credit_card_installment_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE loan
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_loan_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE loan_installment
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_loan_installment_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE loan_entry
  ALTER COLUMN owner_user_id SET NOT NULL,
  ADD CONSTRAINT fk_loan_entry_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (id);

CREATE INDEX idx_financial_account_owner_active_name
  ON financial_account (owner_user_id, active, name);
CREATE INDEX idx_financial_transaction_owner_account_date
  ON financial_transaction (owner_user_id, financial_account_id, occurred_on DESC, created_at DESC);
CREATE INDEX idx_fixed_expense_owner_name
  ON fixed_expense (owner_user_id, name);
CREATE INDEX idx_fixed_expense_occurrence_owner_period
  ON fixed_expense_occurrence (owner_user_id, occurrence_year, occurrence_month);
CREATE INDEX idx_credit_card_owner_active_name
  ON credit_card (owner_user_id, active, name)
  WHERE deleted_at IS NULL;
CREATE INDEX idx_credit_card_purchase_owner_card
  ON credit_card_purchase (owner_user_id, credit_card_id, purchase_date DESC)
  WHERE deleted_at IS NULL;
CREATE INDEX idx_loan_owner_status_direction
  ON loan (owner_user_id, status, direction, name)
  WHERE deleted_at IS NULL;
