# Diretrizes de codigo

## Java e Spring

- Use Java 21, `java.time` e tipagem explicita.
- Prefira composicao e classes pequenas com responsabilidade clara.
- Use injecao por construtor.
- Nao use `System.out.println`; use o logging padrao do Spring.
- Use os niveis de log adequados e nao registre secrets, tokens ou dados financeiros sensiveis.
- Valide entradas na fronteira HTTP com Bean Validation.
- Nao exponha entidades JPA diretamente.
- Nao introduza bibliotecas sem necessidade comprovada.
- Controllers devem depender de contratos em `application/contract`, nao de implementacoes concretas.
- Interfaces devem usar sufixo `Interface`.
- Implementacoes de casos de uso devem ficar em `application/service` e usar sufixo `Impl`.
- Metodos publicos devem ter nomes concretos do caso de uso, como `createFinancialAccount`, evitando nomes genericos como `create`, `update`, `get` e `from`.
- DTOs de transporte HTTP devem ficar em `web/dto/request` ou `web/dto/response` e terminar com `DTO`.
- Nao use `View` para respostas HTTP; prefira nomes explicitos como `FinancialAccountResponseDTO`.
- Controllers nao devem montar DTOs HTTP nem chamar factories. O service chamado pelo controller deve devolver o DTO de resposta ja montado.
- Factories em `web/factory` devem ter metodos concretos de conversao, evitando nomes genericos como `from`.
- Nao crie interfaces, factories, mappers ou classes base apenas por antecipacao.

## Persistencia

- Toda alteracao de schema exige nova migration Flyway.
- Nao altere migrations aplicadas.
- Nunca use `float` ou `double` para dinheiro.
- Consultas e indices devem responder a casos de uso reais.

O formatador padrao do projeto ainda esta pendente; ate sua definicao, preserve o estilo existente e mantenha diffs pequenos.
