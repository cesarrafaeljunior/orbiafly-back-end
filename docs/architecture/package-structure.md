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
    |   |-- application/
    |   `-- web/
    |-- financialaccount/
    |   |-- domain/
    |   |-- application/
    |   |-- infrastructure/persistence/
    |   `-- web/
    `-- fixedexpense/
        |-- domain/
        |-- application/
        |-- infrastructure/persistence/
        `-- web/
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
