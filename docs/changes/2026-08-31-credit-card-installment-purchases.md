# Compras parceladas no cartao - 2026-08-31

## Contexto

O modulo de cartoes precisava registrar compras parceladas sem confundir cartao com conta financeira e sem antecipar faturas ou pagamento de fatura.

## Entrega

- Criado o dominio `CreditCardPurchase` e `CreditCardInstallment`.
- Adicionado endpoint `/api/v1/credit-card-purchases` com overview, detalhe, criacao e soft delete.
- Geradas parcelas no backend a partir de data da compra, fechamento e vencimento do cartao.
- Ajustado limite utilizado/disponivel para derivar das compras nao excluidas; a regra atual permite limite disponivel negativo ao cadastrar compras acima do limite.
- Adicionados testes de integracao com PostgreSQL via Testcontainers.

## Fora do escopo

- Faturas completas.
- Pagamento de fatura.
- Juros, taxas, multas e cashback.
- Antecipacao de parcelas.
- Cartoes adicionais.
- Notificacoes.
