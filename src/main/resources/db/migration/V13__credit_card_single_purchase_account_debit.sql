ALTER TABLE financial_transaction
  DROP CONSTRAINT ck_financial_transaction_reference_type,
  ADD CONSTRAINT ck_financial_transaction_reference_type CHECK (
    reference_type IS NULL OR reference_type IN (
      'FIXED_EXPENSE_OCCURRENCE',
      'INSTALLMENT_EXPENSE_INSTALLMENT',
      'LOAN_ENTRY',
      'EXPECTED_INCOME',
      'ACCOUNT_TRANSFER',
      'CREDIT_CARD_PURCHASE'
    )
  );
