# Regras transversais do dominio

Estas regras orientam o dominio financeiro e incluem o primeiro modulo implementado.

- Valores monetarios nunca devem usar `float` ou `double`.
- Em Java, valores monetarios devem usar `BigDecimal`.
- No PostgreSQL, valores monetarios devem usar `NUMERIC` com precisao explicitamente definida.
- Conversoes devem preservar precisao; arredondamento exige regra de negocio explicita.
- Datas sem horario usam `LocalDate`; timestamps absolutos usam `Instant` e UTC.
- Dados financeiros futuros devem pertencer a um usuario ou contexto de propriedade, mesmo que o primeiro uso seja individual.
- Representacao de moedas e suporte multimoeda serao decididos quando o primeiro caso real surgir.

## Despesas fixas

- Despesas fixas sao recorrencias; pagamento pertence a competencia mensal.
- A configuracao recorrente armazena nome, valor padrao, vencimento, categoria e atividade.
- Uma despesa possui no maximo uma ocorrencia para cada combinacao de ano e mes.
- Uma ocorrencia nova inicia como `PENDING`; ao virar `PAID`, recebe `paidAt` em UTC.
- Voltar para `PENDING` remove `paidAt`.
- O valor de uma ocorrencia pode divergir do valor padrao sem alterar a recorrencia.
- Ocorrencias persistem snapshot de nome, valor, vencimento e categoria para preservar historico.
- Alterar a recorrencia nao reescreve ocorrencias existentes.
- Desativar uma despesa impede novas ocorrencias, mas preserva as ja materializadas.
- Exclusao fisica nao faz parte da operacao normal desta versao.
