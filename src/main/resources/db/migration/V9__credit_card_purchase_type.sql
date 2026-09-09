ALTER TABLE credit_card_purchase
  ADD COLUMN purchase_type VARCHAR(30) NOT NULL DEFAULT 'INSTALLMENT';

ALTER TABLE credit_card_purchase
  ADD CONSTRAINT ck_credit_card_purchase_type CHECK (purchase_type IN ('SINGLE', 'INSTALLMENT'));
