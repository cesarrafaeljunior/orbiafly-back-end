# Banco de dados

PostgreSQL 16.8 e o banco relacional principal. A conexao e externa ao codigo e usa `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`.

## Schema e migrations

Flyway e a unica fonte de evolucao estrutural. Migrations ficam em `src/main/resources/db/migration`, sao imutaveis depois de aplicadas e recebem novas versoes para mudancas posteriores. Hibernate usa `ddl-auto=validate` e nao altera o schema.

`V1__baseline.sql` registra a linha de base tecnica. `V2__fixed_expenses.sql` cria:

- `category`, com categorias iniciais persistidas;
- `fixed_expense`, com a configuracao recorrente;
- `fixed_expense_occurrence`, com snapshot e estado mensal.

Valores usam `NUMERIC(15, 2)`. As constraints validam valores nao negativos, vencimentos entre 1 e 31, meses entre 1 e 12 e status `PENDING` ou `PAID`. A unicidade `(fixed_expense_id, occurrence_year, occurrence_month)` impede duplicidade mensal.

Ocorrencias sao materializadas sob demanda ao consultar uma competencia. Um `INSERT ... ON CONFLICT DO NOTHING` cria apenas as ocorrencias ausentes das despesas ativas. Nao existe scheduler nem operacao de reset.

Atraso, dias ate o vencimento, dias vencidos e percentual pago nao sao persistidos. Esses dados sao derivados na camada de aplicacao a partir da ocorrencia mensal, da competencia e do `Clock` configurado.

`V3__financial_accounts.sql` cria:

- `financial_account`, com dados cadastrais, saldo inicial, atividade e timestamps;
- `financial_transaction`, com tipo, valor, data, descricao e referencias de origem ou estorno;
- referencias opcionais de conta e transacao de pagamento em `fixed_expense_occurrence`.

Valores usam `NUMERIC(15, 2)`. Constraints validam tipos de conta e movimentacao, sinal dos valores e consistencia entre ocorrencia paga, conta e transacao. Indices atendem saldo por conta, historico ordenado e referencias de pagamento. Unicidades impedem mais de um pagamento por ocorrencia e mais de um estorno por debito original.

O saldo nao possui coluna mutavel: e calculado como `opening_balance + SUM(balance_effect)`. Debitos armazenam valor positivo e produzem efeito negativo; creditos produzem efeito positivo; ajustes armazenam diferenca assinada.

`V6__loans.sql` amplia `financial_transaction.reference_type` para aceitar `LOAN_ENTRY` e cria:

- `loan`, com direcao, contraparte, valor total, data inicial, dados opcionais de parcelamento, taxa informativa, notas, status, soft delete e timestamps;
- `loan_installment`, com numero, valor, vencimento, status e data de conclusao;
- `loan_entry`, com valor, data, conta financeira e movimentacao financeira vinculada.

Valores usam `NUMERIC(15, 2)`. Constraints validam direcoes `BORROWED` e `LENT`, status `ACTIVE` e `COMPLETED`, consistencia entre campos de parcelamento e status das parcelas.

Pagamentos de emprestimos tomados geram `DEBIT`; recebimentos de emprestimos cedidos geram `CREDIT`. Ambos ficam vinculados por `reference_type = 'LOAN_ENTRY'` e `reference_id`.

Nenhuma migration foi necessaria para a evolucao visual e contratual de Despesas Fixas em 2026-08-27, porque os novos campos sao derivados de dados ja persistidos.

`V4__credit_cards.sql` cria:

- `credit_card`, com identidade, bandeira, ultimos quatro digitos opcionais, limite total, dias de ciclo, atividade, soft delete e timestamps.

Valores usam `NUMERIC(15, 2)`. Constraints validam bandeiras aceitas, ultimos quatro digitos quando informados, limite nao negativo e dias entre 1 e 31. O indice parcial atende listagem por atividade e nome somente para cartoes nao excluidos.

Limite utilizado e limite disponivel nao sao persistidos. Eles sao derivados pela camada de aplicacao a partir de compras parceladas nao excluidas.

`V5__credit_card_purchases.sql` cria:

- `credit_card_purchase`, com descricao, valor total, data da compra, quantidade de parcelas, valor base da parcela, vinculo ao cartao, soft delete e timestamps;
- `credit_card_installment`, com numero da parcela, valor, competencia, vencimento, status e timestamp.

Valores usam `NUMERIC(15, 2)`. Constraints validam valor positivo, quantidade minima de parcelas, competencia mensal e status `PENDING`. A unicidade `(purchase_id, installment_number)` impede duplicidade de parcelas da mesma compra. Indices atendem consultas por cartao e competencias futuras.

O limite comprometido soma o valor total das compras parceladas nao excluidas. As parcelas sao geradas pelo backend no cadastro da compra; o vencimento mensal respeita o dia de vencimento do cartao, limitado ao ultimo dia valido do mes.

## Convencoes futuras

- IDs: UUID e a estrategia inicial, pela exposicao segura e compatibilidade com evolucao multiusuario. UUID v7 podera ser avaliado quando houver suporte e necessidade concretos.
- Dinheiro: `BigDecimal` no Java e `NUMERIC` com precisao definida no PostgreSQL.
- Tempo: `Instant` para timestamps absolutos em UTC e `LocalDate` para datas sem horario.
- Auditoria: `createdAt` e `updatedAt` serao implementados com a primeira entidade que precisar deles; nao existe `BaseEntity` antecipada.
- Propriedade: dados financeiros futuros nao serao globais e deverao possuir escopo de usuario.

Indices serao definidos a partir de consultas reais e restricoes de integridade, sem antecipacao especulativa.
