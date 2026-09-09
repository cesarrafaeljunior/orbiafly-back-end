CREATE TABLE one_off_expense (
  id UUID PRIMARY KEY,
  owner_user_id UUID NOT NULL REFERENCES app_user (id),
  name VARCHAR(120) NOT NULL,
  amount NUMERIC(15, 2) NOT NULL,
  expense_date DATE NOT NULL,
  category_id UUID NOT NULL REFERENCES category (id),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  paid_at TIMESTAMPTZ,
  financial_account_id UUID REFERENCES financial_account (id),
  payment_transaction_id UUID UNIQUE REFERENCES financial_transaction (id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_one_off_expense_name_not_blank CHECK (btrim(name) <> ''),
  CONSTRAINT ck_one_off_expense_amount CHECK (amount > 0),
  CONSTRAINT ck_one_off_expense_status CHECK (status IN ('PENDING', 'PAID')),
  CONSTRAINT ck_one_off_expense_payment_source CHECK (
    (status = 'PENDING' AND paid_at IS NULL AND financial_account_id IS NULL AND payment_transaction_id IS NULL)
    OR (
      status = 'PAID'
      AND (
        (financial_account_id IS NULL AND payment_transaction_id IS NULL)
        OR (financial_account_id IS NOT NULL AND payment_transaction_id IS NOT NULL)
      )
    )
  )
);

CREATE INDEX idx_one_off_expense_owner_date
  ON one_off_expense (owner_user_id, expense_date DESC, created_at DESC);
CREATE INDEX idx_one_off_expense_account
  ON one_off_expense (financial_account_id)
  WHERE financial_account_id IS NOT NULL;

ALTER TABLE financial_transaction
  DROP CONSTRAINT ck_financial_transaction_reference_type,
  ADD CONSTRAINT ck_financial_transaction_reference_type CHECK (
    reference_type IS NULL OR reference_type IN (
      'FIXED_EXPENSE_OCCURRENCE',
      'INSTALLMENT_EXPENSE_INSTALLMENT',
      'LOAN_ENTRY',
      'EXPECTED_INCOME',
      'ACCOUNT_TRANSFER',
      'CREDIT_CARD_PURCHASE',
      'ONE_OFF_EXPENSE'
    )
  );
