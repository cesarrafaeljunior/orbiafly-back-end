ALTER TABLE financial_transaction
  DROP CONSTRAINT ck_financial_transaction_type,
  ADD CONSTRAINT ck_financial_transaction_type CHECK (
    transaction_type IN ('CREDIT', 'DEBIT', 'ADJUSTMENT', 'TRANSFER_IN', 'TRANSFER_OUT')
  );

ALTER TABLE financial_transaction
  DROP CONSTRAINT ck_financial_transaction_amount,
  ADD CONSTRAINT ck_financial_transaction_amount CHECK (
    (transaction_type IN ('CREDIT', 'DEBIT', 'TRANSFER_IN', 'TRANSFER_OUT') AND amount >= 0)
    OR (transaction_type = 'ADJUSTMENT' AND amount <> 0)
  );

ALTER TABLE financial_transaction
  DROP CONSTRAINT ck_financial_transaction_reference_type,
  ADD CONSTRAINT ck_financial_transaction_reference_type CHECK (
    reference_type IS NULL OR reference_type IN (
      'FIXED_EXPENSE_OCCURRENCE',
      'INSTALLMENT_EXPENSE_INSTALLMENT',
      'LOAN_ENTRY',
      'EXPECTED_INCOME',
      'ACCOUNT_TRANSFER'
    )
  );

CREATE TABLE account_transfer (
  id UUID PRIMARY KEY,
  source_account_id UUID NOT NULL REFERENCES financial_account (id),
  destination_account_id UUID NOT NULL REFERENCES financial_account (id),
  amount NUMERIC(15, 2) NOT NULL,
  transfer_date DATE NOT NULL,
  description VARCHAR(180) NOT NULL,
  source_transaction_id UUID UNIQUE REFERENCES financial_transaction (id),
  destination_transaction_id UUID UNIQUE REFERENCES financial_transaction (id),
  owner_user_id UUID NOT NULL REFERENCES app_user (id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_account_transfer_amount CHECK (amount > 0),
  CONSTRAINT ck_account_transfer_accounts CHECK (source_account_id <> destination_account_id),
  CONSTRAINT ck_account_transfer_description_not_blank CHECK (btrim(description) <> '')
);

CREATE INDEX idx_account_transfer_owner_date
  ON account_transfer (owner_user_id, transfer_date DESC, created_at DESC);
CREATE INDEX idx_account_transfer_source
  ON account_transfer (source_account_id, transfer_date DESC);
CREATE INDEX idx_account_transfer_destination
  ON account_transfer (destination_account_id, transfer_date DESC);
