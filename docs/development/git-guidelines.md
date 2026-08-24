# Diretrizes de Git

- Crie commits pequenos e coerentes, sem misturar refactors alheios.
- Nunca versione `.env`, secrets, logs ou artefatos de `target/`.
- Revise `git diff` e execute testes relevantes antes do commit.
- Mensagens devem explicar a mudanca realizada de forma objetiva.
- Migrations aplicadas sao imutaveis; alteracoes exigem um novo arquivo versionado.
- Atualize `docs/changes` para mudancas relevantes.
