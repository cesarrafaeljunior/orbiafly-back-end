# Diretrizes de testes

A estrategia combina testes unitarios rapidos com integracoes reais onde a infraestrutura influencia o comportamento.

- Testes unitarios cobrem regras de dominio sem subir o Spring quando possivel.
- Testes de repository usam PostgreSQL por Testcontainers.
- Testes HTTP validam contrato, status e validacao nas fronteiras.
- Testes de integracao devem permanecer focados e independentes.
- H2 nao substitui automaticamente PostgreSQL, pois diferencas de SQL e tipos podem mascarar falhas.

O teste inicial `OrbiaflyApplicationTests` usa o profile `test`, inicia PostgreSQL 16.8 com a URL JDBC do Testcontainers, aplica Flyway e valida o contexto Spring.

`FixedExpenseApiTests` cobre contratos HTTP, validacoes, materializacao idempotente, alteracao mensal, status, totais, snapshots historicos e desativacao com PostgreSQL real.

Execute:

```bash
./mvnw test
```

Docker deve estar disponivel para testes que usam Testcontainers.
