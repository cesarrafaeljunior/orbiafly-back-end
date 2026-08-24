# Estrutura de pacotes

Estrutura atual:

```text
src/main/java/com/orbiafly/api/
|-- OrbiaflyApplication.java
|-- infrastructure/
|   `-- web/
`-- modules/
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

Nao devem ser criados pacotes globais `controller`, `service`, `repository`, `entity` e `dto` misturando todos os dominios. Tambem nao devem ser criados pacotes vazios para features futuras.
