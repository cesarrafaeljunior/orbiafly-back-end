# Changelog

## 2026-08-26

- Refatorada a arquitetura interna dos modulos de dashboard, contas financeiras e despesas fixas.
- Separados contratos de aplicacao, implementacoes, modelos internos, controllers, DTOs HTTP e factories de resposta.
- Padronizadas interfaces com sufixo `Interface`.
- Removida a conversao direta de DTOs nos controllers; services agora devolvem os DTOs de resposta.
- Renomeados metodos genericos de services e factories para nomes concretos.
- Padronizados DTOs HTTP com sufixo `DTO` e removido o uso de `View` como resposta de API.
- Preservados endpoints e comportamento funcional existentes, sem novas regras de negocio.
- Adicionada exclusao definitiva de contas financeiras inativas com remocao de movimentacoes e limpeza de vinculos de pagamentos.
- Adicionada reativacao de contas financeiras preservando historico, movimentacoes e vinculos existentes.

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
