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

## ORB-002 - Dashboard mensal - 2026-08-25

- O dashboard e um modulo de leitura sem tabelas, entities ou repositories proprios.
- O modulo consome `FixedExpenseService` como fronteira publica e nao acessa repositories de despesas fixas.
- Uma unica chamada retorna todas as agregacoes exigidas pela interface, evitando multiplas requisicoes e divergencia de regras no frontend.
- A primeira versao consulta somente a competencia selecionada; comparacao com mes anterior foi adiada para nao materializar outra competencia implicitamente.
- Atraso e derivado no momento da consulta com um `Clock` injetavel e fuso configurado por `APP_TIME_ZONE`.
- Proximos vencimentos e maiores despesas possuem limite de cinco itens no contrato.
- Percentuais usam escala 2 e `HALF_UP`; ausencia de total e representada por `null`.
- Nenhuma migration foi necessaria porque o dashboard deriva dados do modelo de despesas fixas existente.

## ORB-003 - Contas e razao de movimentacoes - 2026-08-25

- `FinancialAccount` armazena identidade, tipo, instituicao, saldo inicial e atividade; o saldo atual nao e persistido como cache mutavel.
- `FinancialTransaction` e o razao imutavel de creditos, debitos, ajustes e estornos. O saldo deriva do saldo inicial somado ao efeito dessas movimentacoes.
- Ajuste manual recebe o saldo alvo e registra somente a diferenca assinada, mantendo a reconciliacao auditavel.
- Saldo negativo e permitido porque o sistema registra a realidade financeira e sinaliza o resultado ao consumidor.
- Desativacao substitui exclusao fisica e bloqueia novas movimentacoes sem perder historico.

## ORB-003 - Pagamento atomico e estorno - 2026-08-25

- Pagamento de despesa fixa bloqueia a ocorrencia e a conta dentro da mesma transacao, cria um unico debito e registra as referencias nos dois dominios.
- A unicidade da referencia de pagamento e o bloqueio pessimista protegem contra debitos duplicados em chamadas concorrentes.
- Uma repeticao com a mesma conta e idempotente; conflito com outra origem e respondido como `409`.
- Estorno cria um credito compensatorio ligado ao debito original. Movimentacoes financeiras nao sao apagadas.
- `fixedexpense.application` depende da fronteira publica de `financialaccount.application`; repositories nao atravessam modulos.
- Ocorrencias pagas antes da ORB-003 aceitam conta e transacao nulas para compatibilidade historica.
