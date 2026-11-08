# Desenvolvimento local

## Pre-requisitos

- JDK 21 e Maven 3.6.3 ou superior.
- Docker com Docker Compose.

## Fluxo recomendado

```bash
cp .env.example .env
# Preencha POSTGRES_PASSWORD, PGADMIN_DEFAULT_PASSWORD, APP_JWT_SECRET e APP_ADMIN_*
docker compose up -d postgres pgadmin
./mvnw spring-boot:run
```

Servicos:

- Backend: `http://localhost:4000`.
- Health: `http://localhost:4000/actuator/health`.
- PostgreSQL: `localhost:5487`.
- pgAdmin: `http://localhost:5050`.

O Compose le `.env`. A aplicacao executada diretamente usa os defaults exclusivos de desenvolvimento do profile `local` ou as variaveis `DB_*` exportadas no shell. Se credenciais do PostgreSQL forem alteradas em `.env`, os valores correspondentes devem ser informados ao processo Spring.

## Stack completa

```bash
docker compose --profile full up -d
```

Nesse fluxo, o backend usa `postgres:5432` dentro da network Docker.

## Encerrar

```bash
docker compose down
```

Para apagar tambem o banco local:

```bash
docker compose down -v
```

O segundo comando remove definitivamente os dados armazenados no volume local.
