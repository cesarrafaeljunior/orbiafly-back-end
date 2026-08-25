CREATE TABLE financial_account (
  id UUID PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  account_type VARCHAR(30) NOT NULL,
  institution VARCHAR(120),
  opening_balance NUMERIC(15, 2) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_financial_account_name_not_blank CHECK (btrim(name) <> ''),
  CONSTRAINT ck_financial_account_institution_not_blank CHECK (
    institution IS NULL OR btrim(institution) <> ''
  ),
  CONSTRAINT ck_financial_account_type CHECK (
    account_type IN ('CHECKING', 'SAVINGS', 'DIGITAL_ACCOUNT', 'CASH', 'OTHER')
  )
);

CREATE TABLE financial_transaction (
  id UUID PRIMARY KEY,
  financial_account_id UUID NOT NULL REFERENCES financial_account (id),
  transaction_type VARCHAR(20) NOT NULL,
  amount NUMERIC(15, 2) NOT NULL,
  description VARCHAR(180) NOT NULL,
  occurred_on DATE NOT NULL,
  reference_type VARCHAR(40),
  reference_id UUID,
  reversal_of_id UUID UNIQUE REFERENCES financial_transaction (id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_financial_transaction_type CHECK (
    transaction_type IN ('CREDIT', 'DEBIT', 'ADJUSTMENT')
  ),
  CONSTRAINT ck_financial_transaction_amount CHECK (
    (transaction_type IN ('CREDIT', 'DEBIT') AND amount >= 0)
    OR (transaction_type = 'ADJUSTMENT' AND amount <> 0)
  ),
  CONSTRAINT ck_financial_transaction_description_not_blank CHECK (btrim(description) <> ''),
  CONSTRAINT ck_financial_transaction_reference CHECK (
    (reference_type IS NULL AND reference_id IS NULL)
    OR (reference_type IS NOT NULL AND reference_id IS NOT NULL)
  ),
  CONSTRAINT ck_financial_transaction_reference_type CHECK (
    reference_type IS NULL OR reference_type IN ('FIXED_EXPENSE_OCCURRENCE')
  ),
  CONSTRAINT ck_financial_transaction_reversal CHECK (
    reversal_of_id IS NULL OR transaction_type = 'CREDIT'
  )
);

ALTER TABLE fixed_expense_occurrence
  ADD COLUMN financial_account_id UUID REFERENCES financial_account (id),
  ADD COLUMN payment_transaction_id UUID UNIQUE REFERENCES financial_transaction (id),
  ADD CONSTRAINT ck_fixed_expense_occurrence_payment_source CHECK (
    (status = 'PENDING' AND financial_account_id IS NULL AND payment_transaction_id IS NULL)
    OR (
      status = 'PAID'
      AND (
        (financial_account_id IS NULL AND payment_transaction_id IS NULL)
        OR (financial_account_id IS NOT NULL AND payment_transaction_id IS NOT NULL)
      )
    )
  );

CREATE INDEX idx_financial_account_active_name
  ON financial_account (active, name);
CREATE INDEX idx_financial_transaction_account_date
  ON financial_transaction (financial_account_id, occurred_on DESC, created_at DESC);
CREATE INDEX idx_financial_transaction_reference
  ON financial_transaction (reference_type, reference_id)
  WHERE reference_type IS NOT NULL;
CREATE INDEX idx_fixed_expense_occurrence_financial_account
  ON fixed_expense_occurrence (financial_account_id)
  WHERE financial_account_id IS NOT NULL;
