CREATE TABLE credit_card (
  id UUID PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  brand VARCHAR(30) NOT NULL,
  last_four_digits VARCHAR(4),
  total_limit NUMERIC(15, 2) NOT NULL,
  closing_day INTEGER NOT NULL,
  due_day INTEGER NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_credit_card_name_not_blank CHECK (btrim(name) <> ''),
  CONSTRAINT ck_credit_card_brand CHECK (
    brand IN ('MASTERCARD', 'VISA', 'ELO', 'AMERICAN_EXPRESS', 'HIPERCARD', 'OTHER')
  ),
  CONSTRAINT ck_credit_card_last_four_digits CHECK (
    last_four_digits IS NULL OR last_four_digits ~ '^[0-9]{4}$'
  ),
  CONSTRAINT ck_credit_card_total_limit CHECK (total_limit >= 0),
  CONSTRAINT ck_credit_card_closing_day CHECK (closing_day BETWEEN 1 AND 31),
  CONSTRAINT ck_credit_card_due_day CHECK (due_day BETWEEN 1 AND 31)
);

CREATE INDEX idx_credit_card_active_name_not_deleted
  ON credit_card (active, name)
  WHERE deleted_at IS NULL;
