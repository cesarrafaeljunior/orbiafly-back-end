# Contrato mensal de despesas fixas enriquecido

## Objetivo

Apoiar a nova experiencia mensal de Despesas Fixas sem duplicar regras financeiras no frontend.

## Mudancas

- `GET /api/v1/fixed-expenses/monthly` passou a retornar `totalCount` e `paidPercentage` no resumo.
- Cada item mensal passou a retornar `dueDate`, `overdue`, `daysUntilDue` e `daysOverdue`.
- `overdue` e derivado de ocorrencia pendente com vencimento anterior ao dia atual.
- Datas de vencimento respeitam o ultimo dia valido da competencia quando `dueDay` nao existe no mes.
- O pagamento por conta permanece transacional: valida ocorrencia, valida conta ativa, cria debito, associa transacao e marca a ocorrencia como paga.
- Estorno ao voltar para pendente continua usando movimentacao compensatoria, preservando historico.
- Dashboard nao foi redesenhado e continua consumindo a fronteira de aplicacao de despesas fixas.

## Banco

Nenhuma migration foi necessaria. Os novos campos sao derivados de dados existentes.

## Fora do escopo

Cartao de credito, fatura, parcelamento, receitas, notificacoes e scheduler permanecem fora desta etapa.
