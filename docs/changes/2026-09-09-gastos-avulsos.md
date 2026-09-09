# Gastos avulsos - 2026-09-09

- Adicionado o módulo `oneoffexpense` para despesas únicas, isoladas por usuário.
- Criados endpoints para consulta mensal, cadastro, edição, exclusão, alteração de status e pagamento por conta.
- Pagamentos geram débito rastreável no razão da conta; retorno a pendente gera crédito de estorno.
- Gastos avulsos pendentes passam a compor a projeção financeira mensal.
- O dashboard mensal agrega despesas fixas e gastos avulsos em totais, atrasos, categorias e listas.
- A migration `V15__one_off_expenses.sql` cria a tabela e o tipo de referência financeira `ONE_OFF_EXPENSE`.
