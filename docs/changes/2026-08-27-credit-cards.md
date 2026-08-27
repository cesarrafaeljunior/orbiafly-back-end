# Cartoes de credito - 2026-08-27

## O que mudou

- Criado o dominio `CreditCard`, separado de `FinancialAccount`.
- Adicionada a migration `V4__credit_cards.sql`.
- Criados endpoints para cadastrar, listar, consultar, editar, desativar, reativar e excluir cartoes por soft delete.
- O overview de cartoes retorna limite total, utilizado, disponivel e quantidade de cartoes ativos.
- Limite utilizado e disponivel sao derivados; nesta versao, utilizado e zero e disponivel e igual ao limite total.

## Fora do escopo

- Compras no cartao.
- Compras parceladas.
- Faturas e pagamento de fatura.
- Integracao com contas financeiras.
- Juros, cashback e notificacoes.

## Regra futura registrada

Uma compra parcelada deve comprometer inicialmente o valor total da compra no limite, nao apenas o valor de uma parcela.
