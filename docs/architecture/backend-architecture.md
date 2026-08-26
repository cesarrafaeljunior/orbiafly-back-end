# Arquitetura do backend

O Orbiafly inicia como monolito modular: uma unica aplicacao Spring Boot e um unico processo de deploy, com limites internos organizados por dominio.

## Organizacao

- `modules`: features de negocio, criadas somente quando entrarem no escopo.
- `infrastructure`: configuracoes e adaptadores tecnicos compartilhados pela aplicacao.
- `shared`: elementos realmente compartilhados e estaveis; nao deve virar deposito generico.

Um modulo pode separar `domain`, `application`, `infrastructure` e `web` quando essa divisao reduzir complexidade real. Clean Architecture, arquitetura hexagonal e DDD nao devem ser aplicados de forma cerimonial.

## Limites

- Controllers tratam transporte HTTP e delegam casos de uso.
- Regras de negocio nao ficam em controllers.
- Entidades JPA nao sao expostas diretamente na API.
- DTOs de API pertencem ao pacote `web/dto` e sempre usam sufixo `DTO`.
- Services montam os DTOs de resposta para casos de uso chamados por controllers.
- Objetos internos de aplicacao ficam em `application/model` e nao sao expostos diretamente pelos controllers.
- Controllers dependem de interfaces em `application/contract`, nao de implementacoes concretas.
- Interfaces sempre usam sufixo `Interface`.
- Implementacoes de casos de uso ficam em `application/service` com sufixo `Impl`.
- Factories de resposta em `web/factory` convertem modelos internos em DTOs dentro dos services.
- DTOs de API nao sao usados indiscriminadamente como entidades ou modelos de aplicacao.
- Um modulo nao acessa repositories de outro modulo arbitrariamente.
- Dependencias circulares, services gigantes e classes `Utils` genericas devem ser evitados.
- Persistencia nao deve determinar todo o modelo de dominio.

Microservicos e infraestrutura distribuida nao fazem parte da arquitetura atual.

## Estrutura interna de modulo

Quando um modulo possui controllers, casos de uso e persistencia, a direcao esperada e:

```text
<modulo>/
|-- application/
|   |-- contract/
|   |-- factory/
|   |-- model/
|   `-- service/
|-- domain/
|-- infrastructure/persistence/
`-- web/
    |-- controller/
    |-- dto/
    |   |-- request/
    |   `-- response/
    `-- factory/
```

Crie apenas pacotes que possuam classes reais. O objetivo da estrutura e deixar claro onde ficam contratos, implementacoes, modelos internos e DTOs de transporte.

## Modulo de despesas fixas

O primeiro modulo fica em `modules/fixedexpense` e separa:

- `domain`: entidades e status de pagamento;
- `application`: contratos, implementacoes de casos de uso e modelos internos;
- `infrastructure/persistence`: repositories Spring Data;
- `web`: controllers, DTOs HTTP, factories de resposta e validacao dos contratos HTTP.

O modulo usa um service de aplicacao como fronteira transacional. Controllers dependem de `FixedExpenseServiceInterface`, nao de `FixedExpenseServiceImpl`. Controllers nao acessam repositories, nao montam DTOs de resposta e entidades JPA nao sao expostas pela API.

## Modulo de dashboard

`modules/dashboard` e um modulo de leitura sem persistencia propria. Sua implementacao consome os contratos publicos de `FixedExpenseServiceInterface` e `FinancialAccountServiceInterface`, agrega os dados em memoria e retorna o DTO de dashboard para o controller. A conversao final fica no service, usando factory de resposta.

Essa dependencia e unidirecional: dashboard depende das fronteiras de aplicacao, nao de seus repositories. A consulta mensal materializa a competencia e carrega ocorrencias e categorias em uma unica operacao, evitando consultas por item.

A data atual vem de um `Clock` configurado em `infrastructure/time`, o que torna a regra de atraso deterministica em testes e explicita o fuso local.

## Modulo de contas financeiras

`modules/financialaccount` separa dominio, aplicacao, persistencia e web. O contrato `FinancialAccountServiceInterface` e a fronteira para cadastro, saldo derivado, movimentacoes, ajuste e desativacao; `FinancialAccountServiceImpl` contem a implementacao transacional.

Pagamentos conectam despesas fixas e contas pela camada de aplicacao. `FixedExpenseServiceInterface` coordena a transacao e usa `FinancialAccountServiceInterface`; nao acessa repositories do outro modulo. Locks pessimistas e constraints de unicidade protegem a operacao concorrente, enquanto estornos criam movimentacoes compensatorias auditaveis.
