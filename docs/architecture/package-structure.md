# Estrutura de pacotes

Estrutura atual:

```text
src/main/java/com/orbiafly/api/
|-- OrbiaflyApplication.java
|-- infrastructure/
|   |-- time/
|   `-- web/
`-- modules/
    |-- dashboard/
    |   |-- application/contract/
    |   |-- application/model/
    |   |-- application/service/
    |   |-- web/controller/
    |   |-- web/dto/response/
    |   `-- web/factory/
    |-- financialaccount/
    |   |-- application/contract/
    |   |-- application/factory/
    |   |-- application/model/
    |   |-- application/service/
    |   |-- domain/
    |   |-- infrastructure/persistence/
    |   |-- web/controller/
    |   |-- web/dto/request/
    |   |-- web/dto/response/
    |   `-- web/factory/
    `-- fixedexpense/
        |-- application/contract/
        |-- application/factory/
        |-- application/model/
        |-- application/service/
        |-- domain/
        |-- infrastructure/persistence/
        |-- web/controller/
        |-- web/dto/request/
        |-- web/dto/response/
        `-- web/factory/
```

A estrutura cresce somente com codigo necessario. Quando features forem autorizadas, a direcao planejada e:

```text
com/orbiafly/api/
|-- shared/
|-- infrastructure/
`-- modules/
    `-- <dominio>/
        |-- domain/
        |-- application/
        `-- infrastructure/
```

`dashboard` e um modulo de leitura e, por nao possuir persistencia ou entidades proprias, usa somente `application` e `web`. Ele consome as fronteiras publicas de `fixedexpense` e `financialaccount`.

Nao devem ser criados pacotes globais `controller`, `service`, `repository`, `entity` e `dto` misturando todos os dominios. Tambem nao devem ser criados pacotes vazios para features futuras.

## Convencoes por camada

- `application/contract`: interfaces consumidas por controllers ou outros modulos.
- interfaces Java usam sufixo `Interface`.
- `application/service`: implementacoes transacionais dos casos de uso, nomeadas com sufixo `Impl`.
- `application/model`: modelos internos usados pelos services. Nao sao DTOs HTTP.
- `application/factory`: construcao de modelos internos a partir de entidades quando isso reduzir conversao repetida no service.
- `web/controller`: controllers Spring MVC.
- `web/dto/request`: contratos de entrada HTTP com Bean Validation e sufixo `RequestDTO`.
- `web/dto/response`: contratos de saida HTTP com sufixo `ResponseDTO`.
- `web/factory`: conversao de modelos internos para DTOs de resposta, chamada pelos services.

Controllers nao chamam factories nem fazem conversao manual de entidades ou modelos internos para DTOs. Eles devem chamar metodos concretos dos contratos de aplicacao e retornar o DTO recebido.
