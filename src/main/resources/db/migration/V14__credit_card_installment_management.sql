ALTER TABLE credit_card_installment
  ADD COLUMN IF NOT EXISTS paid_at TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE credit_card_installment
  DROP CONSTRAINT IF EXISTS ck_credit_card_installment_status;

ALTER TABLE credit_card_installment
  ADD CONSTRAINT ck_credit_card_installment_status CHECK (status IN ('PENDING', 'PAID'));

CREATE INDEX IF NOT EXISTS idx_credit_card_installment_owner_period_status
  ON credit_card_installment (owner_user_id, competence_year, competence_month, status);
