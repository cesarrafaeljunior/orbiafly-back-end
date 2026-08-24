# Infraestrutura local

A infraestrutura local do Orbiafly usa Docker Compose para PostgreSQL e pgAdmin.

O fluxo recomendado de desenvolvimento e executar o backend na maquina local e manter apenas os servicos de apoio em containers:

```bash
cp .env.example .env
docker compose up -d postgres pgadmin
./mvnw spring-boot:run
```

Tambem existe um profile opcional para subir a stack completa:

```bash
docker compose --profile full up -d
```

Para remover containers e o volume local do PostgreSQL:

```bash
docker compose down -v
```

Esse comando remove os dados locais do banco.
