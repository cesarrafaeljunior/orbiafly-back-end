# Docker

O arquivo `compose.yml` define os servicos de desenvolvimento.

## Servicos

- `postgres`: PostgreSQL 16.8, porta local 5432, volume persistente e healthcheck com `pg_isready`.
- `pgadmin`: pgAdmin 9.8, porta local 5050 e inicializacao apos o banco ficar saudavel.
- `backend`: build opcional pelo profile `full`, porta local 8080 e dependencia do PostgreSQL saudavel.

Todos usam a network `orbiafly-network`. Entre containers, o banco e acessado por `postgres:5432`, nunca por `localhost`.

O volume nomeado `orbiafly_postgres_data` nao fica dentro do repositorio e sobrevive a recriacao dos containers. `docker compose down -v` remove esse volume e seus dados.

## Imagem do backend

O `Dockerfile` usa build multi-stage com Maven e Java 21. A imagem final contem somente o JRE e o JAR, executado por usuario nao-root. Secrets nao fazem parte da imagem.

O `.dockerignore` exclui metadados do Git, documentacao, arquivos locais de ambiente e artefatos de build do contexto enviado ao Docker.

As imagens possuem tags explicitas; `latest` nao e permitido.
