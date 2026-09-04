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
- A data de vencimento mensal e derivada de ano, mes e dia de vencimento; se o dia nao existir na competencia, usa o ultimo dia do mes.
- Uma ocorrencia vencida e uma ocorrencia `PENDING` com data de vencimento anterior ao dia atual no fuso configurado.
- Atraso e informacao derivada da consulta mensal; nao existe status persistido para vencida.
- O resumo mensal retorna total, pago, pendente, quantidade total, quantidade paga, quantidade pendente e percentual financeiro pago.
- Quando o total da competencia e zero, o percentual financeiro pago retorna `0.00` no contrato de despesas fixas.
- O valor de uma ocorrencia pode divergir do valor padrao sem alterar a recorrencia.
- Ocorrencias persistem snapshot de nome, valor, vencimento e categoria para preservar historico.
- Alterar a recorrencia nao reescreve ocorrencias existentes.
- Desativar uma despesa impede novas ocorrencias, mas preserva as ja materializadas.
- Exclusao fisica nao faz parte da operacao normal desta versao.

## Dashboard mensal

- Os indicadores mensais do dashboard representam ocorrencias de despesas fixas; o saldo disponivel representa contas ativas e nao depende da competencia.
- Total, pago, pendente e quantidades derivam da mesma consulta mensal usada pelo modulo de despesas fixas.
- Uma despesa esta vencida quando permanece `PENDING` e sua data de vencimento e anterior a data atual no fuso configurado.
- Uma despesa pendente com vencimento no dia atual nao esta vencida e pode aparecer nos proximos vencimentos.
- Vencimentos acima do ultimo dia do mes sao ajustados para o ultimo dia da competencia.
- Proximos vencimentos sao ordenados por data e nome e limitados a cinco itens.
- Maiores despesas sao ordenadas por valor decrescente e nome e limitadas a cinco itens.
- Categorias sao ordenadas por total decrescente; percentuais usam duas casas e arredondamento `HALF_UP`.
- O percentual pago e `null` quando o total da competencia e zero.
- Comparacao com a competencia anterior nao faz parte desta versao.

## Contas financeiras e movimentacoes

- Conta financeira representa uma origem real de dinheiro: conta corrente, poupanca, conta digital, dinheiro em especie ou outra conta equivalente.
- O saldo atual e sempre derivado do saldo inicial mais os efeitos de todas as movimentacoes.
- `CREDIT` aumenta o saldo, `DEBIT` diminui e `ADJUSTMENT` aplica a diferenca assinada necessaria para atingir o saldo informado.
- Saldo negativo e permitido e informado explicitamente nas respostas de pagamento.
- O saldo inicial e imutavel depois do cadastro; correcoes usam ajustes rastreaveis.
- Desativacao impede novas movimentacoes e preserva a conta e seu historico.
- O pagamento de uma despesa fixa cria um debito atomico vinculado a ocorrencia e a uma conta ativa.
- Repetir o mesmo pagamento para a mesma ocorrencia e conta retorna o resultado existente, sem novo debito.
- Retornar uma ocorrencia paga para `PENDING` cria um credito de estorno vinculado ao debito original.
- Ocorrencias historicas pagas sem conta de origem permanecem validas.
- Forma de pagamento futura podera aceitar cartao de credito, mas nesta etapa pagamento de despesa fixa usa apenas `FinancialAccount`.

## Cartoes de credito

- Cartao de credito nao e conta financeira; `FinancialAccount` representa dinheiro disponivel e `CreditCard` representa credito/divida.
- Cartoes armazenam apenas nome, bandeira, ultimos quatro digitos opcionais, limite total, dia de fechamento, dia de vencimento, status, soft delete e timestamps.
- Numero completo, CVV, senha e PIN nunca devem ser armazenados.
- O usuario informa somente o limite total. Limite utilizado e limite disponivel sao derivados.
- Limite utilizado e limite disponivel sao derivados das compras parceladas ativas e nao sao persistidos como campos editaveis.
- Desativar um cartao impede uso futuro e preserva o registro. Reativar torna o cartao ativo novamente.
- Excluir cartao aplica soft delete por `deletedAt`; consultas normais nao retornam cartoes excluidos.
- Compra no cartao nao reduz imediatamente o saldo de conta financeira.
- Pagamento de fatura reduz a conta escolhida e nao cria nova despesa alem das compras registradas.
- Compra parcelada so pode ser registrada em cartao ativo e nao excluido.
- O valor total da compra parcelada compromete o limite imediatamente, nao apenas a primeira parcela.
- Parcelas sao geradas pelo backend com base na data da compra, dia de fechamento e dia de vencimento do cartao.
- Compra ate o dia de fechamento entra na competencia do mes da compra; compra depois do fechamento entra na competencia seguinte.
- Dia de vencimento inexistente em uma competencia usa o ultimo dia valido do mes.
- Arredondamento de parcelas preserva exatamente a soma do valor total; a ultima parcela absorve diferencas de centavos.
- Compras parceladas usam soft delete; compras excluidas deixam de comprometer limite e nao aparecem em consultas normais.
- Faturas, pagamento de fatura, juros, multas, taxas, antecipacao de parcelas, cartoes adicionais, cashback e notificacoes permanecem fora do escopo atual.

## Emprestimos

- Emprestimo representa dinheiro que o usuario pegou emprestado (`BORROWED`) ou emprestou para outra pessoa/instituicao (`LENT`).
- A contraparte e texto livre e identifica quem emprestou ou recebeu o dinheiro.
- O valor total cadastrado e o valor real do compromisso; calculo financeiro complexo de juros nao faz parte desta versao.
- Taxa de juros, quando informada, e apenas texto informativo.
- Emprestimos podem ter cronograma parcelado ou nao possuir parcelamento definido.
- Quando parcelado, o backend gera parcelas automaticamente a partir da quantidade, valor da parcela e primeiro vencimento.
- Diferenças de centavos sao absorvidas pela ultima parcela para que a soma seja igual ao valor total.
- Quando nao ha cronograma, pagamentos ou recebimentos parciais sao permitidos ate quitar o saldo.
- Total pago/recebido, saldo restante, percentual de progresso e previsao de termino sao derivados de parcelas e lancamentos.
- `COMPLETED` e derivado quando o saldo restante chega a zero.
- Pagamento de `BORROWED` cria debito transacional em uma conta financeira ativa.
- Recebimento de `LENT` cria credito transacional em uma conta financeira ativa.
- Cartao de credito nao participa de emprestimos nesta versao.
- Exclusao usa soft delete; movimentacoes financeiras ja registradas permanecem no historico da conta.
- Cobranca automatica, notificacoes, contratos, correcao monetaria, SAC/Price, renegociacao, integracao bancaria externa e juros compostos avancados ficam fora do escopo.
