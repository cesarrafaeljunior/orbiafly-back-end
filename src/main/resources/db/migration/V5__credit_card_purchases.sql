CREATE TABLE credit_card_purchase (
  id UUID PRIMARY KEY,
  credit_card_id UUID NOT NULL REFERENCES credit_card (id),
  description VARCHAR(160) NOT NULL,
  total_amount NUMERIC(15, 2) NOT NULL,
  purchase_date DATE NOT NULL,
  installment_count INTEGER NOT NULL,
  installment_amount NUMERIC(15, 2) NOT NULL,
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_credit_card_purchase_description_not_blank CHECK (btrim(description) <> ''),
  CONSTRAINT ck_credit_card_purchase_total_amount CHECK (total_amount > 0),
  CONSTRAINT ck_credit_card_purchase_installment_count CHECK (installment_count >= 1),
  CONSTRAINT ck_credit_card_purchase_installment_amount CHECK (installment_amount > 0)
);

CREATE TABLE credit_card_installment (
  id UUID PRIMARY KEY,
  purchase_id UUID NOT NULL REFERENCES credit_card_purchase (id),
  installment_number INTEGER NOT NULL,
  amount NUMERIC(15, 2) NOT NULL,
  competence_year INTEGER NOT NULL,
  competence_month INTEGER NOT NULL,
  due_date DATE NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_credit_card_installment_number CHECK (installment_number >= 1),
  CONSTRAINT ck_credit_card_installment_amount CHECK (amount > 0),
  CONSTRAINT ck_credit_card_installment_month CHECK (competence_month BETWEEN 1 AND 12),
  CONSTRAINT ck_credit_card_installment_status CHECK (status IN ('PENDING')),
  CONSTRAINT uk_credit_card_installment_purchase_number UNIQUE (purchase_id, installment_number)
);

CREATE INDEX idx_credit_card_purchase_card_not_deleted
  ON credit_card_purchase (credit_card_id, purchase_date DESC)
  WHERE deleted_at IS NULL;

CREATE INDEX idx_credit_card_installment_competence
  ON credit_card_installment (competence_year, competence_month);
