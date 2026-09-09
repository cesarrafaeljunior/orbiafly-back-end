ALTER TABLE financial_transaction
  DROP CONSTRAINT ck_financial_transaction_reference_type,
  ADD CONSTRAINT ck_financial_transaction_reference_type CHECK (
    reference_type IS NULL OR reference_type IN (
      'FIXED_EXPENSE_OCCURRENCE',
      'INSTALLMENT_EXPENSE_INSTALLMENT',
      'LOAN_ENTRY',
      'EXPECTED_INCOME'
    )
  );

CREATE TABLE expected_income (
  id UUID PRIMARY KEY,
  description VARCHAR(160) NOT NULL,
  amount NUMERIC(15, 2) NOT NULL,
  expected_date DATE NOT NULL,
  financial_account_id UUID REFERENCES financial_account (id),
  status VARCHAR(20) NOT NULL,
  received_at TIMESTAMPTZ,
  financial_transaction_id UUID UNIQUE REFERENCES financial_transaction (id),
  notes VARCHAR(500),
  owner_user_id UUID NOT NULL REFERENCES app_user (id),
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_expected_income_description_not_blank CHECK (btrim(description) <> ''),
  CONSTRAINT ck_expected_income_amount CHECK (amount > 0),
  CONSTRAINT ck_expected_income_status CHECK (status IN ('PENDING', 'RECEIVED', 'CANCELED')),
  CONSTRAINT ck_expected_income_notes_not_blank CHECK (notes IS NULL OR btrim(notes) <> ''),
  CONSTRAINT ck_expected_income_receipt CHECK (
    (status = 'RECEIVED' AND received_at IS NOT NULL AND financial_transaction_id IS NOT NULL)
    OR (status <> 'RECEIVED' AND received_at IS NULL AND financial_transaction_id IS NULL)
  )
);

CREATE INDEX idx_expected_income_owner_status_date
  ON expected_income (owner_user_id, status, expected_date)
  WHERE deleted_at IS NULL;
CREATE INDEX idx_expected_income_owner_date
  ON expected_income (owner_user_id, expected_date)
  WHERE deleted_at IS NULL;
CREATE INDEX idx_expected_income_account
  ON expected_income (financial_account_id)
  WHERE financial_account_id IS NOT NULL;
