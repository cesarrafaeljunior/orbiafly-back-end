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
- Nao crie interfaces, factories, mappers ou classes base apenas por antecipacao.

## Persistencia

- Toda alteracao de schema exige nova migration Flyway.
- Nao altere migrations aplicadas.
- Nunca use `float` ou `double` para dinheiro.
- Consultas e indices devem responder a casos de uso reais.

O formatador padrao do projeto ainda esta pendente; ate sua definicao, preserve o estilo existente e mantenha diffs pequenos.
