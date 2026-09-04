ALTER TABLE financial_transaction
  DROP CONSTRAINT ck_financial_transaction_reference_type,
  ADD CONSTRAINT ck_financial_transaction_reference_type CHECK (
    reference_type IS NULL OR reference_type IN (
      'FIXED_EXPENSE_OCCURRENCE',
      'INSTALLMENT_EXPENSE_INSTALLMENT',
      'LOAN_ENTRY'
    )
  );

CREATE TABLE loan (
  id UUID PRIMARY KEY,
  name VARCHAR(140) NOT NULL,
  direction VARCHAR(20) NOT NULL,
  counterparty VARCHAR(140) NOT NULL,
  total_amount NUMERIC(15, 2) NOT NULL,
  start_date DATE NOT NULL,
  installment_count INTEGER,
  installment_amount NUMERIC(15, 2),
  first_due_date DATE,
  interest_rate VARCHAR(60),
  notes VARCHAR(500),
  status VARCHAR(20) NOT NULL,
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_loan_name_not_blank CHECK (btrim(name) <> ''),
  CONSTRAINT ck_loan_counterparty_not_blank CHECK (btrim(counterparty) <> ''),
  CONSTRAINT ck_loan_direction CHECK (direction IN ('BORROWED', 'LENT')),
  CONSTRAINT ck_loan_total_amount CHECK (total_amount > 0),
  CONSTRAINT ck_loan_status CHECK (status IN ('ACTIVE', 'COMPLETED')),
  CONSTRAINT ck_loan_installment_count CHECK (installment_count IS NULL OR installment_count >= 1),
  CONSTRAINT ck_loan_installment_amount CHECK (installment_amount IS NULL OR installment_amount > 0),
  CONSTRAINT ck_loan_installment_shape CHECK (
    (installment_count IS NULL AND installment_amount IS NULL AND first_due_date IS NULL)
    OR (installment_count IS NOT NULL AND installment_amount IS NOT NULL AND first_due_date IS NOT NULL)
  ),
  CONSTRAINT ck_loan_interest_rate_not_blank CHECK (interest_rate IS NULL OR btrim(interest_rate) <> ''),
  CONSTRAINT ck_loan_notes_not_blank CHECK (notes IS NULL OR btrim(notes) <> '')
);

CREATE TABLE loan_installment (
  id UUID PRIMARY KEY,
  loan_id UUID NOT NULL REFERENCES loan (id),
  installment_number INTEGER NOT NULL,
  amount NUMERIC(15, 2) NOT NULL,
  due_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  completed_at DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_loan_installment_number CHECK (installment_number >= 1),
  CONSTRAINT ck_loan_installment_amount CHECK (amount > 0),
  CONSTRAINT ck_loan_installment_status CHECK (status IN ('PENDING', 'COMPLETED')),
  CONSTRAINT ck_loan_installment_completion CHECK (
    (status = 'PENDING' AND completed_at IS NULL)
    OR (status = 'COMPLETED' AND completed_at IS NOT NULL)
  ),
  CONSTRAINT uk_loan_installment_number UNIQUE (loan_id, installment_number)
);

CREATE TABLE loan_entry (
  id UUID PRIMARY KEY,
  loan_id UUID NOT NULL REFERENCES loan (id),
  installment_id UUID REFERENCES loan_installment (id),
  amount NUMERIC(15, 2) NOT NULL,
  occurred_on DATE NOT NULL,
  financial_account_id UUID NOT NULL REFERENCES financial_account (id),
  financial_transaction_id UUID UNIQUE NOT NULL REFERENCES financial_transaction (id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_loan_entry_amount CHECK (amount > 0)
);

CREATE INDEX idx_loan_status_direction_not_deleted
  ON loan (status, direction, name)
  WHERE deleted_at IS NULL;
CREATE INDEX idx_loan_installment_loan_status_due
  ON loan_installment (loan_id, status, due_date);
CREATE INDEX idx_loan_entry_loan_date
  ON loan_entry (loan_id, occurred_on DESC, created_at DESC);
