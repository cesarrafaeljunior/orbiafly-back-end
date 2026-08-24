CREATE TABLE category (
  id UUID PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_category_name_not_blank CHECK (btrim(name) <> '')
);

CREATE TABLE fixed_expense (
  id UUID PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  default_amount NUMERIC(15, 2) NOT NULL,
  due_day INTEGER NOT NULL,
  category_id UUID NOT NULL REFERENCES category (id),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_fixed_expense_name_not_blank CHECK (btrim(name) <> ''),
  CONSTRAINT ck_fixed_expense_amount_non_negative CHECK (default_amount >= 0),
  CONSTRAINT ck_fixed_expense_due_day CHECK (due_day BETWEEN 1 AND 31)
);

CREATE TABLE fixed_expense_occurrence (
  id UUID PRIMARY KEY,
  fixed_expense_id UUID NOT NULL REFERENCES fixed_expense (id),
  occurrence_year INTEGER NOT NULL,
  occurrence_month INTEGER NOT NULL,
  name VARCHAR(120) NOT NULL,
  amount NUMERIC(15, 2) NOT NULL,
  due_day INTEGER NOT NULL,
  category_id UUID NOT NULL,
  category_name VARCHAR(80) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  paid_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_fixed_expense_occurrence_period UNIQUE (fixed_expense_id, occurrence_year, occurrence_month),
  CONSTRAINT ck_fixed_expense_occurrence_year CHECK (occurrence_year BETWEEN 2000 AND 2200),
  CONSTRAINT ck_fixed_expense_occurrence_month CHECK (occurrence_month BETWEEN 1 AND 12),
  CONSTRAINT ck_fixed_expense_occurrence_name_not_blank CHECK (btrim(name) <> ''),
  CONSTRAINT ck_fixed_expense_occurrence_amount_non_negative CHECK (amount >= 0),
  CONSTRAINT ck_fixed_expense_occurrence_due_day CHECK (due_day BETWEEN 1 AND 31),
  CONSTRAINT ck_fixed_expense_occurrence_status CHECK (status IN ('PENDING', 'PAID')),
  CONSTRAINT ck_fixed_expense_occurrence_paid_at CHECK (
    (status = 'PAID' AND paid_at IS NOT NULL)
    OR (status = 'PENDING' AND paid_at IS NULL)
  )
);

CREATE INDEX idx_fixed_expense_category ON fixed_expense (category_id);
CREATE INDEX idx_fixed_expense_occurrence_period
  ON fixed_expense_occurrence (occurrence_year, occurrence_month, due_day);

INSERT INTO category (id, name) VALUES
  ('00000000-0000-0000-0000-000000000001', 'Moradia'),
  ('00000000-0000-0000-0000-000000000002', 'Empréstimo'),
  ('00000000-0000-0000-0000-000000000003', 'Plano celular'),
  ('00000000-0000-0000-0000-000000000004', 'Casa'),
  ('00000000-0000-0000-0000-000000000005', 'Financiamentos'),
  ('00000000-0000-0000-0000-000000000006', 'Assinaturas'),
  ('00000000-0000-0000-0000-000000000007', 'Automóvel'),
  ('00000000-0000-0000-0000-000000000008', 'Pets'),
  ('00000000-0000-0000-0000-000000000009', 'Saúde'),
  ('00000000-0000-0000-0000-000000000010', 'Alimentação'),
  ('00000000-0000-0000-0000-000000000011', 'Impostos');
