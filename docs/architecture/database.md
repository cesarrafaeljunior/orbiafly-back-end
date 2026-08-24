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

## Convencoes futuras

- IDs: UUID e a estrategia inicial, pela exposicao segura e compatibilidade com evolucao multiusuario. UUID v7 podera ser avaliado quando houver suporte e necessidade concretos.
- Dinheiro: `BigDecimal` no Java e `NUMERIC` com precisao definida no PostgreSQL.
- Tempo: `Instant` para timestamps absolutos em UTC e `LocalDate` para datas sem horario.
- Auditoria: `createdAt` e `updatedAt` serao implementados com a primeira entidade que precisar deles; nao existe `BaseEntity` antecipada.
- Propriedade: dados financeiros futuros nao serao globais e deverao possuir escopo de usuario.

Indices serao definidos a partir de consultas reais e restricoes de integridade, sem antecipacao especulativa.
