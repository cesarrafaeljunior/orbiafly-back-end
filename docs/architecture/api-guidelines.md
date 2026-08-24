# Convencoes de API

APIs usam o prefixo `/api/v1`.

## HTTP

- `GET` consulta recursos sem efeitos colaterais.
- `POST` cria recursos ou inicia operacoes nao idempotentes.
- `PUT` substitui uma representacao; `PATCH` altera parcialmente.
- `DELETE` remove quando permitido pelo dominio.
- Status codes devem representar o resultado: `200`, `201`, `204`, `400`, `404`, `409` e `422`, conforme o caso.

Entradas externas devem ser validadas com Bean Validation. Entidades JPA nao devem ser retornadas diretamente.

## Erros

Erros futuros seguirao RFC 9457 Problem Details, usando `type`, `title`, `status`, `detail` e `instance`. Respostas nao devem expor stack traces, SQL, secrets ou detalhes internos.

## Listagens

Paginacao e filtros devem ser explicitos, possuir limites maximos e ordenacao previsivel. Filtros entram apenas quando houver casos de uso reais.

Mudancas incompativeis exigem nova versao da API. Mudancas aditivas devem preservar consumidores existentes sempre que possivel.

## CORS

Origins permitidas sao configuradas por `APP_CORS_ALLOWED_ORIGINS`. O default local aceita `http://localhost:4200`; producao deve informar origins exatas e nunca usar `*` indiscriminadamente.

## Despesas fixas

| Metodo | Caminho | Uso |
| --- | --- | --- |
| `GET` | `/api/v1/categories` | Lista categorias persistidas. |
| `GET` | `/api/v1/fixed-expenses` | Lista configuracoes recorrentes. |
| `POST` | `/api/v1/fixed-expenses` | Cria uma despesa ativa. |
| `PUT` | `/api/v1/fixed-expenses/{id}` | Atualiza configuracao e atividade. |
| `GET` | `/api/v1/fixed-expenses/monthly?year=&month=` | Materializa e retorna a competencia com totais. |
| `PATCH` | `/api/v1/fixed-expense-occurrences/{id}/amount` | Altera somente o valor mensal. |
| `PATCH` | `/api/v1/fixed-expense-occurrences/{id}/status` | Alterna entre `PENDING` e `PAID`. |

A listagem mensal e ordenada por vencimento e nome. Nao possui paginacao porque o volume esperado e pessoal e limitado.
