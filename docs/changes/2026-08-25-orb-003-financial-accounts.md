# ORB-003 - Contas financeiras e pagamentos

## Alteracoes

- Criados os modelos de conta e movimentacao com saldo derivado.
- Expostas operacoes de cadastro, edicao, desativacao, consulta de historico e ajuste.
- Integrado o pagamento de despesa fixa a um debito atomico e seu retorno para pendente a um credito de estorno.
- Incluido saldo das contas ativas no dashboard mensal.
- Adicionada a migration V3 com constraints, indices e referencias financeiras.

## Compatibilidade

Ocorrencias historicas pagas sem conta de origem permanecem validas. O endpoint antigo de status continua aceitando esse legado, mas novos pagamentos exigem conta enquanto houver contas ativas.

## Verificacao

- Suite Maven com PostgreSQL real via Testcontainers.
- Testes HTTP de contas, movimentacoes, pagamento, idempotencia, estorno e dashboard.
