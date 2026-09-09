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
- Desativacao preserva historico e bloqueia novas movimentacoes. Reativacao preserva os mesmos dados e devolve a conta aos totais ativos. Exclusao fisica so e exposta para contas ja desativadas.
- Filtros de status, categoria e nome ficam no frontend apos a carga da competencia completa.

## ORB-002 - Dashboard mensal - 2026-08-25

- O dashboard e um modulo de leitura sem tabelas, entities ou repositories proprios.
- O modulo consome `FixedExpenseServiceInterface` como fronteira publica e nao acessa repositories de despesas fixas.
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
- Desativacao bloqueia novas movimentacoes sem perder historico. Reativacao torna a conta ativa novamente sem apagar movimentacoes ou vinculos. Contas inativas podem ser excluidas definitivamente quando o usuario decidir remover tambem as movimentacoes e vinculos financeiros associados.

## ORB-003 - Pagamento atomico e estorno - 2026-08-25

- Pagamento de despesa fixa bloqueia a ocorrencia e a conta dentro da mesma transacao, cria um unico debito e registra as referencias nos dois dominios.
- A unicidade da referencia de pagamento e o bloqueio pessimista protegem contra debitos duplicados em chamadas concorrentes.
- Uma repeticao com a mesma conta e idempotente; conflito com outra origem e respondido como `409`.
- Estorno cria um credito compensatorio ligado ao debito original. Movimentacoes financeiras nao sao apagadas.
- `fixedexpense.application` depende da fronteira publica de `financialaccount.application`; repositories nao atravessam modulos.
- Ocorrencias pagas antes da ORB-003 aceitam conta e transacao nulas para compatibilidade historica.
- Exclusao definitiva de conta inativa volta ocorrencias pagas por essa conta para pendente, remove a referencia financeira e apaga as movimentacoes antes de remover a conta.

## ORB - Contrato mensal enriquecido de despesas fixas - 2026-08-27

- O endpoint mensal de despesas fixas preserva os campos existentes e adiciona contagem total, percentual financeiro pago, data de vencimento e informacoes derivadas de atraso.
- `OVERDUE` nao foi persistido como status. Uma ocorrencia vencida continua sendo `PENDING` com `dueDate` anterior ao dia atual no fuso da aplicacao.
- Vencimentos em dia inexistente na competencia usam o ultimo dia valido do mes.
- O percentual pago usa escala 2 e `HALF_UP`; quando o total e zero, o contrato mensal de despesas retorna `0.00`.
- O Dashboard preserva sua semantica anterior e continua retornando `null` para percentual sem total.
- Nenhuma migration foi criada porque os novos dados sao derivados de ano, mes, vencimento, status e valor.
- Cartao de credito permanece fora do escopo; pagamento de despesa fixa nesta etapa usa somente `FinancialAccount`.

## ORB - Cartoes de credito - 2026-08-27

- `CreditCard` foi criado em modulo proprio, separado de `FinancialAccount`, para preservar a distincao entre dinheiro disponivel e credito/divida.
- Cartoes usam soft delete por `deletedAt`; consultas normais excluem registros deletados e podem incluir inativos por parametro.
- Limite utilizado e limite disponivel fazem parte do contrato da API, mas sao derivados. Nesta primeira versao, sem compras, utilizado e zero e disponivel e igual ao limite total.
- A API retorna overview e listagem na mesma chamada para evitar chamadas paralelas na tela.
- Dados sensiveis de cartao, como numero completo, CVV, senha ou PIN, nao fazem parte do modelo.
- Compras, parcelamentos, faturas, pagamento de fatura e integracao com contas financeiras foram adiados.
- Regra futura registrada: compras parceladas devem comprometer o valor total da compra no limite no momento inicial.

## ORB - Compras parceladas no cartao - 2026-08-31

- `CreditCardPurchase` e `CreditCardInstallment` foram adicionados ao modulo de cartoes, mantendo `CreditCard` como agregador de limite e ciclo.
- O cadastro de compra bloqueia o cartao e valida cartao ativo e nao excluido antes de persistir.
- Limite utilizado e disponivel continuam derivados; compras nao excluidas comprometem o valor total da compra e o limite disponivel pode ficar negativo.
- Parcelas sao materializadas no cadastro para preservar competencia, vencimento e valor de cada parcela.
- A regra de ciclo fica centralizada no backend: compras ate o fechamento usam a competencia da compra; compras apos o fechamento iniciam na competencia seguinte.
- A divisao monetaria usa duas casas com arredondamento para baixo nas parcelas regulares e ajuste de centavos na ultima parcela.
- A API expõe overview, listagem, detalhe, criacao e soft delete em `/api/v1/credit-card-purchases`.
- Fatura, pagamento de fatura, juros, taxas, antecipacao, cartoes adicionais e notificacoes permanecem fora do escopo.

## ORB - Emprestimos - 2026-08-31

- `Loan` foi criado como modulo unico para dinheiro tomado e dinheiro emprestado, diferenciado por `LoanDirection`.
- Contraparte permanece texto livre para evitar cadastro prematuro de pessoas e instituicoes.
- Parcelas sao materializadas no cadastro somente quando ha cronograma definido; emprestimos sem cronograma aceitam lancamentos parciais.
- Pagamentos e recebimentos usam `LoanEntry` como historico de dominio e `FinancialTransaction` como razao financeiro da conta.
- A integracao com contas usa a fronteira publica de `FinancialAccountServiceInterface`; repositories de contas nao atravessam o modulo de emprestimos.
- `FinancialReferenceType.LOAN_ENTRY` identifica movimentacoes geradas por emprestimos.
- Saldo restante, progresso, proxima parcela, previsao de termino e status de quitado sao derivados de parcelas e lancamentos.
- Cartao de credito, cobranca automatica, notificacoes, contratos, SAC/Price, renegociacao e juros compostos avancados permanecem fora do escopo.

## Refatoracao arquitetural de modulos - 2026-08-26

- Controllers passam a ficar em `web/controller` e dependem somente de contratos em `application/contract`.
- Todas as interfaces Java usam sufixo `Interface`.
- Implementacoes concretas de casos de uso ficam em `application/service` com sufixo `Impl`.
- Modelos internos retornados por casos de uso ficam em `application/model` e usam sufixo `Result` quando representam saidas da aplicacao.
- DTOs HTTP ficam em `web/dto/request` e `web/dto/response`, sempre com sufixo `DTO`.
- Controllers nao fazem conversao para DTOs de resposta e nao chamam factories.
- Services chamados por controllers devolvem DTOs HTTP ja montados.
- Factories em `web/factory` convertem modelos internos para DTOs de resposta dentro dos services.
- Metodos publicos de services e factories usam nomes concretos do caso de uso ou conversao, evitando nomes genericos como `create`, `update`, `get` e `from`.
- Factories em `application/factory` podem montar modelos internos a partir de entidades quando isso reduzir conversao repetida no service.
- Nenhuma regra financeira, endpoint funcional ou schema de banco foi alterado nesta refatoracao.


## Recuperacao e alteracao de senha - 2026-09-06

- Troca de senha autenticada exige a senha atual e atualiza apenas o hash armazenado.
- Recuperacao de senha usa codigo numerico de 6 digitos, validade de 15 minutos e uso unico.
- O codigo de recuperacao e armazenado somente como hash BCrypt.
- Solicitacoes de recuperacao retornam sucesso mesmo quando a conta nao existe, evitando enumeracao de usuarios.
- Nesta etapa, o envio gratuito do codigo e feito no log da aplicacao por `PasswordResetCodeSenderInterface`; SMTP ou outro provedor deve substituir essa fronteira quando houver infraestrutura de envio.

## ORB - Gastos avulsos - 2026-09-09

- Gasto avulso e persistido como lancamento unico com data completa, categoria, valor e status; nao gera ocorrencias futuras.
- Pagamento segue o razao imutavel de contas: cria debito com referencia ONE_OFF_EXPENSE e retorno a pendente cria credito de estorno.
- Edicao e exclusao exigem status pendente para preservar a coerencia do historico financeiro.
- Consulta e resumo sao mensais; atraso e derivado da data, do status e do Clock da aplicacao.
- Dashboard agrega despesas fixas e avulsas; projecao inclui somente gastos avulsos pendentes.
- Exclusao de conta inativa desvincula pagamentos avulsos antes de remover suas movimentacoes.
