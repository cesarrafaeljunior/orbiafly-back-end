# Fluxo para agentes

1. Leia `AGENTS.md` e a documentacao relacionada a tarefa.
2. Inspecione o estado do repositorio e preserve alteracoes existentes.
3. Confirme o escopo e nao implemente features adjacentes.
4. Reutilize padroes e dependencias existentes.
5. Atualize documentacao e decisoes quando a mudanca exigir.
6. Registre mudancas relevantes em `docs/changes`.
7. Execute testes, build e validacoes proporcionais ao impacto.
8. Revise `git status` e `git diff` antes da entrega.

Para novas mudancas em modulos com API HTTP:

- mantenha controllers em `web/controller`;
- mantenha requests e responses em `web/dto` com sufixo `DTO`;
- exponha casos de uso por interfaces em `application/contract`;
- use sufixo `Interface` em toda interface;
- implemente casos de uso em `application/service`;
- nao converta respostas diretamente em controllers;
- retorne dos services os DTOs esperados pelos controllers;
- crie factories de resposta em `web/factory` quando houver conversao entre modelos internos e contrato HTTP, e chame essas factories no service.

Resultados nao executados devem ser informados como pendencia; nunca presuma sucesso de Docker, build ou testes.
