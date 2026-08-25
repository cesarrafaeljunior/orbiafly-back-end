# ORB-002 - Dashboard inicial

## Data

2026-08-25.

## Objetivo

Fornecer em uma unica chamada os indicadores mensais necessarios para o dashboard inicial, mantendo as regras financeiras no backend.

## API

Foi adicionado `GET /api/v1/dashboard/monthly?year=&month=`. O contrato retorna competencia, resumo financeiro, vencidos, proximos vencimentos, distribuicao por categoria e maiores despesas.

## Regras

- A fonte de dados e exclusivamente a competencia de despesas fixas.
- Vencidas sao ocorrencias pendentes anteriores a data atual no fuso configurado.
- Proximos vencimentos incluem o dia atual e sao limitados a cinco.
- Maiores despesas sao limitadas a cinco.
- Percentuais usam duas casas e `HALF_UP`; total zero produz percentual `null`.
- Dias inexistentes no mes sao ajustados para o ultimo dia da competencia.

## Arquitetura

O modulo `dashboard` nao possui persistencia. Ele consome a fronteira de aplicacao de despesas fixas e agrega a representacao mensal ja carregada. Um `Clock` configuravel torna a data atual explicita e testavel.

## Integracao com o frontend

A feature Angular faz uma unica requisicao por competencia e recebe os calculos prontos. O layout apresenta indicadores, progresso, situacao, categorias, agenda e ranking sem reproduzir regras financeiras no cliente.

## Infraestrutura

`APP_TIME_ZONE` define o fuso da regra de atraso e usa `America/Sao_Paulo` como default local. Nenhuma migration foi adicionada.

## Testes

Testes cobrem agregacoes completas, ordenacao e limites, atraso, competencia vazia, percentual nulo e vencimento no ultimo dia de fevereiro.

## Limites

- O dashboard nao inclui receitas, saldo, contas, cartoes ou transacoes avulsas.
- Comparacao com a competencia anterior fica para evolucao futura.

## Pendencias

- Reavaliar comparacao mensal quando a regra de produto estiver definida.
- Ampliar o dashboard somente quando receitas, contas ou outros dominios possuirem dados persistidos e contratos proprios.
