# ORB-001 - Gerenciamento de despesas fixas

## Data

2026-08-23.

## Objetivo

Entregar o primeiro vertical slice financeiro do Orbiafly para cadastrar despesas recorrentes e acompanhar cada competencia sem reset mensal.

## Funcionalidade

- Cadastro, listagem e edicao de despesas fixas.
- Desativacao sem apagar historico.
- Categorias persistidas e semeadas.
- Consulta mensal ordenada por vencimento.
- Alteracao de valor somente na competencia.
- Status `PENDING` e `PAID`, com `paidAt` reversivel.
- Resumo com totais e quantidades.

## Banco e migration

`V2__fixed_expenses.sql` cria `category`, `fixed_expense` e `fixed_expense_occurrence`, com UUIDs, FKs, constraints monetarias e de periodo, indices e unicidade mensal.

## API

Foram adicionados endpoints em `/api/v1/categories`, `/api/v1/fixed-expenses` e `/api/v1/fixed-expense-occurrences`. Erros de validacao seguem Problem Details e incluem violacoes por campo.

## Decisoes

- Despesas fixas sao recorrencias; pagamento pertence a competencia mensal.
- Ocorrencias sao materializadas sob demanda e preservam snapshots.
- Nao existe exclusao fisica, scheduler ou reset de competencia.
- Administracao de categorias fica fora desta versao.

## Testes

Testes de integracao com PostgreSQL 16.8 cobrem criacao, validacoes, competencia unica, valor mensal, pagamento, retorno para pendente, snapshot historico, desativacao e totais.

## Pendencias

- Definir periodos efetivos de inicio e fim caso o produto precise criar recorrencias retroativas ou agendadas.
- Adicionar administracao de categorias em tarefa propria.
- Adicionar escopo de usuario junto da futura autenticacao.
