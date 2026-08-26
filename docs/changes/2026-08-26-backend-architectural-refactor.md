# Refatoracao arquitetural do backend - 2026-08-26

## Escopo

Refatoracao interna dos modulos existentes para separar melhor responsabilidades entre `web`, `application`, `domain` e `infrastructure`.

## Mudancas

- Controllers movidos para `web/controller`.
- Requests movidos para `web/dto/request` com sufixo `RequestDTO`.
- Responses movidos para `web/dto/response` com sufixo `ResponseDTO`.
- Contratos de aplicacao criados em `application/contract`.
- Interfaces renomeadas com sufixo `Interface`.
- Implementacoes concretas movidas para `application/service` com sufixo `Impl`.
- Modelos internos de caso de uso movidos para `application/model`.
- Factories de resposta criadas em `web/factory`.
- Factories de modelos internos criadas em `application/factory` quando havia conversao repetida de entidades.
- Controllers deixaram de montar DTOs de resposta diretamente; services devolvem o DTO pronto para o controller.
- Metodos publicos de services e factories foram renomeados para nomes concretos.
- Documentacao de arquitetura, estrutura de pacotes, diretrizes de codigo e fluxo para agentes atualizada.

## Garantias

- Nenhum endpoint funcional novo foi criado.
- Nenhuma regra financeira foi alterada.
- Nenhuma migration foi adicionada.
- O contrato JSON publico foi preservado.

## Validacao

- `mvn -DskipTests compile` em container JDK 21: passou.
- `mvn test-compile` em container JDK 21: passou.
- `mvn -Dtest=DashboardServiceTests test` em container JDK 21: passou.
- `./mvnw test` local nao foi conclusivo porque o ambiente local nao possui `javac` compativel com `--release 21`.
- `mvn test` em container sem socket Docker nao foi conclusivo porque Testcontainers exige acesso a um ambiente Docker valido.
