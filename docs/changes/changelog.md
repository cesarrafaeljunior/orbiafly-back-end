# Changelog

## 2026-08-25

- Implementada a ORB-003 com contas financeiras, saldo derivado, movimentacoes, ajuste e desativacao.
- Pagamentos de despesas fixas agora criam debitos atomicos e estornos compensatorios vinculados a conta.
- Adicionadas migration V3, integracao do saldo ao dashboard e cobertura de integracao com PostgreSQL.
- Implementada a ORB-002 com endpoint mensal agregado para o dashboard.
- Adicionadas regras de atraso por fuso, proximos vencimentos, categorias, maiores despesas e percentual pago.
- Adicionados testes deterministas com `Clock` fixo e documentacao do contrato.

## 2026-08-23

- Criada a fundacao do backend Spring Boot e da infraestrutura local com PostgreSQL, pgAdmin, Flyway, Actuator, Testcontainers e Docker Compose.
- Registradas arquitetura, convencoes de API, banco, testes, seguranca e fluxo de desenvolvimento.
- Implementada a ORB-001 com categorias, despesas fixas, ocorrencias mensais, API REST, validacoes e testes de integracao.
- Adicionada a migration `V2__fixed_expenses.sql` e registrada a estrategia de materializacao sob demanda com snapshot historico.
