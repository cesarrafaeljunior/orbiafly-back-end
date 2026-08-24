# Decisoes tecnicas

## Fundacao inicial - 2026-08-23

- Java 21 foi selecionado como LTS.
- Spring Boot 3.5.16 foi selecionado como linha estavel compativel com Java 21.
- Maven e o sistema de build.
- O backend e um monolito modular organizado por dominio.
- PostgreSQL 16.8 e o banco principal; H2 nao substitui PostgreSQL em testes de persistencia.
- Flyway controla o schema e Hibernate usa `ddl-auto=validate`.
- Testcontainers 1.20.4 fornece PostgreSQL real no teste de contexto.
- Actuator expoe somente `health` nesta etapa.
- CORS local e configuravel por environment variable.
- UUID e a estrategia inicial para IDs futuros; nao existe gerador customizado.
- Auditoria sera criada com a primeira necessidade concreta, sem `BaseEntity` antecipada.
- Spring Security, autenticacao, JWT e autorizacao foram adiados.
- Microservicos e componentes distribuidos nao fazem parte da arquitetura atual.

## ORB-001 - Despesas fixas - 2026-08-23

- Despesas fixas sao recorrencias; pagamento pertence a competencia mensal.
- `FixedExpense` representa configuracao e `FixedExpenseOccurrence` representa ano e mes.
- Ocorrencias sao materializadas sob demanda na primeira consulta da competencia, sem scheduler.
- A insercao em lote usa a unicidade do banco e `ON CONFLICT DO NOTHING` para ser idempotente.
- Ocorrencias guardam snapshot de nome, valor, vencimento e categoria; mudancas recorrentes nao reescrevem historico.
- Categorias sao persistidas e semeadas pela migration V2. Administracao de categorias foi adiada.
- Desativacao e a operacao normal. Exclusao fisica nao foi exposta para proteger historico.
- Filtros de status, categoria e nome ficam no frontend apos a carga da competencia completa.
