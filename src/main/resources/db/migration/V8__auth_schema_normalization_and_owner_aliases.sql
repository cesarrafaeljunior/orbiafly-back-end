ALTER TABLE app_user ADD COLUMN IF NOT EXISTS password_hash VARCHAR(120);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE app_user
SET password_hash = password
WHERE password_hash IS NULL;

ALTER TABLE app_user
  ALTER COLUMN password_hash SET NOT NULL;

ALTER TABLE app_user ALTER COLUMN password DROP NOT NULL;
ALTER TABLE app_user ALTER COLUMN role DROP NOT NULL;

CREATE TABLE IF NOT EXISTS app_user_role (
  user_id UUID NOT NULL REFERENCES app_user (id),
  role VARCHAR(30) NOT NULL,
  CONSTRAINT pk_app_user_role PRIMARY KEY (user_id, role),
  CONSTRAINT ck_app_user_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

INSERT INTO app_user_role (user_id, role)
SELECT id, role
FROM app_user
ON CONFLICT DO NOTHING;

INSERT INTO app_user_role (user_id, role)
SELECT id, 'ROLE_USER'
FROM app_user
WHERE username = 'admin'
ON CONFLICT DO NOTHING;

INSERT INTO app_user_role (user_id, role)
SELECT id, 'ROLE_ADMIN'
FROM app_user
WHERE username = 'admin'
ON CONFLICT DO NOTHING;

ALTER TABLE financial_account ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE financial_transaction ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE fixed_expense ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE fixed_expense_occurrence ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE credit_card ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE installment_purchase ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE installment_expense_installment ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE credit_card_purchase ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE credit_card_installment ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE loan ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE loan_installment ADD COLUMN IF NOT EXISTS owner_user_id UUID;
ALTER TABLE loan_entry ADD COLUMN IF NOT EXISTS owner_user_id UUID;

UPDATE financial_account SET owner_user_id = user_id WHERE owner_user_id IS NULL;
UPDATE financial_transaction SET owner_user_id = user_id WHERE owner_user_id IS NULL;
UPDATE fixed_expense SET owner_user_id = user_id WHERE owner_user_id IS NULL;
UPDATE fixed_expense_occurrence SET owner_user_id = user_id WHERE owner_user_id IS NULL;
UPDATE credit_card SET owner_user_id = user_id WHERE owner_user_id IS NULL;
UPDATE installment_purchase SET owner_user_id = user_id WHERE owner_user_id IS NULL;
UPDATE installment_expense_installment SET owner_user_id = user_id WHERE owner_user_id IS NULL;

UPDATE credit_card_purchase
SET owner_user_id = '00000000-0000-0000-0000-000000000100'
WHERE owner_user_id IS NULL;
UPDATE credit_card_installment
SET owner_user_id = '00000000-0000-0000-0000-000000000100'
WHERE owner_user_id IS NULL;
UPDATE loan
SET owner_user_id = '00000000-0000-0000-0000-000000000100'
WHERE owner_user_id IS NULL;
UPDATE loan_installment
SET owner_user_id = '00000000-0000-0000-0000-000000000100'
WHERE owner_user_id IS NULL;
UPDATE loan_entry
SET owner_user_id = '00000000-0000-0000-0000-000000000100'
WHERE owner_user_id IS NULL;

ALTER TABLE financial_account ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE financial_transaction ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE fixed_expense ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE fixed_expense_occurrence ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE credit_card ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE installment_purchase ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE installment_expense_installment ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE credit_card_purchase ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE credit_card_installment ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE loan ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE loan_installment ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE loan_entry ALTER COLUMN owner_user_id SET NOT NULL;

ALTER TABLE financial_account ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE financial_transaction ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE fixed_expense ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE fixed_expense_occurrence ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE credit_card ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE installment_purchase ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE installment_expense_installment ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE financial_account
  ADD CONSTRAINT fk_financial_account_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE financial_transaction
  ADD CONSTRAINT fk_financial_transaction_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE fixed_expense
  ADD CONSTRAINT fk_fixed_expense_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE fixed_expense_occurrence
  ADD CONSTRAINT fk_fixed_expense_occurrence_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE credit_card
  ADD CONSTRAINT fk_credit_card_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE installment_purchase
  ADD CONSTRAINT fk_installment_purchase_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE installment_expense_installment
  ADD CONSTRAINT fk_installment_expense_installment_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE credit_card_purchase
  ADD CONSTRAINT fk_credit_card_purchase_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE credit_card_installment
  ADD CONSTRAINT fk_credit_card_installment_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE loan
  ADD CONSTRAINT fk_loan_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE loan_installment
  ADD CONSTRAINT fk_loan_installment_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
ALTER TABLE loan_entry
  ADD CONSTRAINT fk_loan_entry_owner_alias FOREIGN KEY (owner_user_id) REFERENCES app_user (id);

CREATE INDEX IF NOT EXISTS idx_financial_account_owner_active_name
  ON financial_account (owner_user_id, active, name);
CREATE INDEX IF NOT EXISTS idx_financial_transaction_owner_account_date
  ON financial_transaction (owner_user_id, financial_account_id, occurred_on DESC, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_fixed_expense_owner_name
  ON fixed_expense (owner_user_id, name);
CREATE INDEX IF NOT EXISTS idx_fixed_expense_occurrence_owner_period
  ON fixed_expense_occurrence (owner_user_id, occurrence_year, occurrence_month);
CREATE INDEX IF NOT EXISTS idx_credit_card_owner_active_name
  ON credit_card (owner_user_id, active, name)
  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_installment_purchase_owner_created
  ON installment_purchase (owner_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_installment_due_owner_status
  ON installment_expense_installment (owner_user_id, status, due_date);
CREATE INDEX IF NOT EXISTS idx_credit_card_purchase_owner_card
  ON credit_card_purchase (owner_user_id, credit_card_id, purchase_date DESC)
  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_loan_owner_status_direction
  ON loan (owner_user_id, status, direction, name)
  WHERE deleted_at IS NULL;
