# Orbiafly Backend

Backend e infraestrutura local do Orbiafly, uma aplicacao de organizacao financeira pessoal. O backend gerencia contas, movimentacoes, despesas fixas, pagamentos e o resumo mensal usado pelo dashboard.

## Stack

- Java 21 e Spring Boot 3.5.16.
- Maven, Spring Web, Spring Data JPA e Bean Validation.
- PostgreSQL 16.8 e Flyway.
- Actuator, Spring Boot Test e Testcontainers 1.20.4.
- Docker Compose e pgAdmin 9.8.

## Pre-requisitos

- JDK 21.
- Maven 3.6.3 ou superior.
- Docker e Docker Compose.

## Configuracao local

```bash
cp .env.example .env
docker compose up -d postgres pgadmin
./mvnw spring-boot:run
```

O backend fica em `http://localhost:8080` e o healthcheck em `http://localhost:8080/actuator/health`.

Os endpoints das features ficam sob `/api/v1/accounts`, `/api/v1/fixed-expenses`, `/api/v1/fixed-expense-occurrences`, `/api/v1/categories` e `/api/v1/dashboard/monthly`.

O PostgreSQL fica em `localhost:5432`. O pgAdmin fica em `http://localhost:5050` e acessa o banco pelo host `postgres`, porta `5432`.

## Testes e build

```bash
./mvnw test
./mvnw verify
```

Os testes de integracao usam PostgreSQL via Testcontainers e, portanto, precisam do Docker em execucao.

## Stack completa em Docker

O fluxo recomendado usa o backend local e os servicos de apoio em containers. A stack completa tambem pode ser iniciada:

```bash
docker compose --profile full up -d
```

Para remover containers e apagar o volume local do PostgreSQL:

```bash
docker compose down -v
```

Esse comando remove todos os dados locais do banco.

## Documentacao

- [Visao do produto](docs/product/overview.md)
- [Arquitetura do backend](docs/architecture/backend-architecture.md)
- [Desenvolvimento local](docs/infrastructure/local-development.md)
- [Variaveis de ambiente](docs/infrastructure/environment-variables.md)
- [Diretrizes de testes](docs/development/testing-guidelines.md)
- [Historico de mudancas](docs/changes/changelog.md)
