CREATE TABLE app_user (
  id UUID PRIMARY KEY,
  username VARCHAR(80) NOT NULL UNIQUE,
  email VARCHAR(160) NOT NULL UNIQUE,
  password VARCHAR(120) NOT NULL,
  role VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_app_user_username_not_blank CHECK (btrim(username) <> ''),
  CONSTRAINT ck_app_user_email_not_blank CHECK (btrim(email) <> ''),
  CONSTRAINT ck_app_user_password_not_blank CHECK (btrim(password) <> ''),
  CONSTRAINT ck_app_user_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

INSERT INTO app_user (id, username, email, password, role)
VALUES (
  '00000000-0000-0000-0000-000000000100',
  'admin',
  'admin@admin.com.br',
  '$2b$12$fDAIrjQfZPYPCMrjy0bcjOvDRzw4MDbef7JOTAFjiA35cngNt6jF6',
  'ROLE_ADMIN'
)
ON CONFLICT (id) DO NOTHING;

ALTER TABLE financial_account ADD COLUMN user_id UUID;
UPDATE financial_account SET user_id = '00000000-0000-0000-0000-000000000100' WHERE user_id IS NULL;
ALTER TABLE financial_account
  ALTER COLUMN user_id SET NOT NULL,
  ADD CONSTRAINT fk_financial_account_user FOREIGN KEY (user_id) REFERENCES app_user (id);

ALTER TABLE financial_transaction ADD COLUMN user_id UUID;
UPDATE financial_transaction transaction
SET user_id = account.user_id
FROM financial_account account
WHERE transaction.financial_account_id = account.id AND transaction.user_id IS NULL;
ALTER TABLE financial_transaction
  ALTER COLUMN user_id SET NOT NULL,
  ADD CONSTRAINT fk_financial_transaction_user FOREIGN KEY (user_id) REFERENCES app_user (id);
ALTER TABLE financial_transaction DROP CONSTRAINT ck_financial_transaction_reference_type;
ALTER TABLE financial_transaction
  ADD CONSTRAINT ck_financial_transaction_reference_type CHECK (
    reference_type IS NULL OR reference_type IN ('FIXED_EXPENSE_OCCURRENCE', 'INSTALLMENT_EXPENSE_INSTALLMENT')
  );

ALTER TABLE fixed_expense ADD COLUMN user_id UUID;
UPDATE fixed_expense SET user_id = '00000000-0000-0000-0000-000000000100' WHERE user_id IS NULL;
ALTER TABLE fixed_expense
  ALTER COLUMN user_id SET NOT NULL,
  ADD CONSTRAINT fk_fixed_expense_user FOREIGN KEY (user_id) REFERENCES app_user (id);

ALTER TABLE fixed_expense_occurrence ADD COLUMN user_id UUID;
UPDATE fixed_expense_occurrence occurrence
SET user_id = expense.user_id
FROM fixed_expense expense
WHERE occurrence.fixed_expense_id = expense.id AND occurrence.user_id IS NULL;
ALTER TABLE fixed_expense_occurrence
  ALTER COLUMN user_id SET NOT NULL,
  ADD CONSTRAINT fk_fixed_expense_occurrence_user FOREIGN KEY (user_id) REFERENCES app_user (id);

ALTER TABLE credit_card ADD COLUMN user_id UUID;
UPDATE credit_card SET user_id = '00000000-0000-0000-0000-000000000100' WHERE user_id IS NULL;
ALTER TABLE credit_card
  ALTER COLUMN user_id SET NOT NULL,
  ADD CONSTRAINT fk_credit_card_user FOREIGN KEY (user_id) REFERENCES app_user (id);

CREATE TABLE installment_purchase (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES app_user (id),
  description VARCHAR(180) NOT NULL,
  total_amount NUMERIC(15, 2) NOT NULL,
  installment_count INTEGER NOT NULL,
  first_installment_date DATE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_installment_purchase_description_not_blank CHECK (btrim(description) <> ''),
  CONSTRAINT ck_installment_purchase_total_amount CHECK (total_amount > 0),
  CONSTRAINT ck_installment_purchase_count CHECK (installment_count BETWEEN 1 AND 120)
);

CREATE TABLE installment_expense_installment (
  id UUID PRIMARY KEY,
  purchase_id UUID NOT NULL REFERENCES installment_purchase (id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES app_user (id),
  installment_number INTEGER NOT NULL,
  amount NUMERIC(15, 2) NOT NULL,
  due_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  paid_at TIMESTAMPTZ,
  financial_account_id UUID REFERENCES financial_account (id),
  payment_transaction_id UUID UNIQUE REFERENCES financial_transaction (id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_installment_purchase_number UNIQUE (purchase_id, installment_number),
  CONSTRAINT ck_installment_number CHECK (installment_number >= 1),
  CONSTRAINT ck_installment_amount CHECK (amount >= 0),
  CONSTRAINT ck_installment_status CHECK (status IN ('PENDING', 'PAID')),
  CONSTRAINT ck_installment_payment_source CHECK (
    (status = 'PENDING' AND paid_at IS NULL AND financial_account_id IS NULL AND payment_transaction_id IS NULL)
    OR (status = 'PAID' AND paid_at IS NOT NULL AND financial_account_id IS NOT NULL AND payment_transaction_id IS NOT NULL)
  )
);

CREATE INDEX idx_app_user_email ON app_user (lower(email));
CREATE INDEX idx_app_user_username ON app_user (lower(username));
CREATE INDEX idx_financial_account_user_active_name ON financial_account (user_id, active, name);
CREATE INDEX idx_financial_transaction_user_account_date ON financial_transaction (user_id, financial_account_id, occurred_on DESC, created_at DESC);
CREATE INDEX idx_fixed_expense_user_name ON fixed_expense (user_id, name);
CREATE INDEX idx_fixed_expense_occurrence_user_period ON fixed_expense_occurrence (user_id, occurrence_year, occurrence_month, due_day);
CREATE INDEX idx_credit_card_user_active_name ON credit_card (user_id, active, name) WHERE deleted_at IS NULL;
CREATE INDEX idx_installment_purchase_user_created ON installment_purchase (user_id, created_at DESC);
CREATE INDEX idx_installment_due_status ON installment_expense_installment (user_id, status, due_date);
