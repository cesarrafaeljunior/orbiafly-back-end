# Orbiafly - Contrato para agentes

## Contexto

Orbiafly e uma aplicacao de organizacao financeira pessoal. O produto comeca focado em uso individual, mas deve permitir evolucao futura para um produto multiusuario.

Este repositorio contem o backend e a infraestrutura local do Orbiafly.

## Stack

- Java 21.
- Spring Boot 3.5.16 e Maven.
- Spring Web, Spring Data JPA e Bean Validation.
- PostgreSQL e Flyway.
- Spring Boot Actuator e Spring Boot Test.
- Testcontainers para integracoes reais com PostgreSQL.
- Docker Compose com PostgreSQL e pgAdmin.

## Arquitetura

O backend comeca como monolito modular: uma unica aplicacao Spring Boot, organizada por dominio quando as features forem implementadas.

Nao introduza microservicos, Kafka, Redis, RabbitMQ, Kubernetes, CQRS ou Event Sourcing sem necessidade real e decisao registrada. Nao crie diretorios vazios apenas por antecipacao.

Modulos com API HTTP devem manter:

- contratos de aplicacao em `application/contract`;
- implementacoes em `application/service`;
- modelos internos em `application/model`;
- controllers em `web/controller`;
- DTOs HTTP em `web/dto/request` e `web/dto/response`, sempre com sufixo `DTO`;
- factories de resposta em `web/factory`.

Interfaces devem usar sufixo `Interface`. Controllers devem depender de contratos de aplicacao, nao de implementacoes concretas. Controllers nao devem fazer conversao para DTO de resposta nem chamar factories; devem delegar o caso de uso ao service e retornar o DTO ja montado pelo service. Metodos publicos devem usar nomes concretos de caso de uso, evitando nomes genericos como `create`, `update`, `get` e `from`.

## Principios

- Respeite estritamente o escopo da tarefa.
- Evite overengineering, abstracoes prematuras e dependencias desnecessarias.
- Evite refactors nao relacionados.
- Nao altere decisoes arquiteturais silenciosamente.
- Mantenha dominios organizados, coesos e testaveis.
- Preserve compatibilidade com os consumidores da API.
- Atualize documentacao relevante ao mudar arquitetura, infraestrutura ou processo.
- Nao implemente funcionalidades financeiras sem pedido explicito.

## Banco

- PostgreSQL e o banco relacional principal.
- Flyway e obrigatorio para mudancas de schema.
- Nao use `spring.jpa.hibernate.ddl-auto=update`.
- Nao modifique migrations ja aplicadas; crie novas migrations versionadas.
- Nunca use `float` ou `double` para valores monetarios.
- Em Java, use `BigDecimal`; no PostgreSQL, use `NUMERIC` com precisao definida.

## Seguranca

- Nao versione secrets.
- Nao logue senhas, tokens ou credenciais.
- Nao exponha informacoes sensiveis em erros HTTP.
- Valide dados externos e aplique menor privilegio.
- Nao implemente autenticacao, JWT ou autorizacao sem escopo explicito.

## Docker

- Nao use tags `latest`.
- Nao coloque secrets no Dockerfile.
- Use versoes explicitas e imagens reproduziveis.
- Preserve volumes locais por padrao.
- Documente mudancas de infraestrutura.

## Alteracoes

Toda alteracao significativa deve:

1. respeitar este `AGENTS.md`;
2. respeitar a documentacao existente;
3. atualizar a documentacao quando necessario;
4. registrar decisoes arquiteturais em `docs/architecture/technical-decisions.md`;
5. registrar a mudanca em `docs/changes`;
6. permanecer dentro do escopo da tarefa.
