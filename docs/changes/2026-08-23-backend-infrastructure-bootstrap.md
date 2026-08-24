# Fundacao do backend e infraestrutura local

## Data

2026-08-23.

## Objetivo e contexto

Estabelecer uma base executavel para o backend do Orbiafly, separada do frontend, sem implementar features financeiras.

## Arquivos criados

- Projeto Maven, aplicacao Spring Boot, profiles e migration baseline.
- Dockerfile, `.dockerignore`, `compose.yml`, `.env.example` e `.gitignore`.
- Configuracao de CORS, teste minimo e documentacao em `docs/`.
- Contrato operacional `AGENTS.md`.

## Arquivos modificados

- `README.md`, substituindo o titulo inicial por instrucoes objetivas de uso.

## Decisoes

- Java 21, Spring Boot 3.5.16, Maven e monolito modular.
- PostgreSQL 16.8 com Flyway e `ddl-auto=validate`.
- Actuator restrito ao healthcheck.
- Testcontainers com PostgreSQL real nos testes de integracao.
- UUID como estrategia inicial de IDs futuros.
- Autenticacao e qualquer dominio financeiro permanecem adiados.

## Infraestrutura e impactos

O desenvolvimento recomendado executa Spring Boot localmente e PostgreSQL/pgAdmin em Docker. Um profile `full` permite construir a aplicacao em container. O volume do banco e persistente e a network e isolada.

## Validacoes executadas

- Build da imagem do backend com Maven e Java 21.
- Teste de integracao com PostgreSQL 16.8 via Testcontainers.
- Validacao e aplicacao da migration baseline pelo Flyway.
- Inicializacao dos servicos `postgres`, `pgadmin` e `backend` pelo Docker Compose.
- Healthcheck da API com resposta `UP` apos a recriacao da imagem final.

## Pendencias

- Definir formatacao automatica de Java quando houver necessidade de padronizacao em equipe.
- Avaliar estrategia de autenticacao somente em tarefa propria.
- Criar o primeiro modulo apenas quando uma feature financeira for autorizada.

## Proximos passos possiveis

Validar a fundacao em CI simples e, em uma tarefa futura, detalhar o primeiro recorte de dominio antes de criar entidades ou endpoints.
