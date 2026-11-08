# Orbiafly — Backend

**API e regras de negócio do Orbiafly, uma aplicação pessoal de gestão financeira executada em ambiente próprio.**

Este repositório concentra o backend responsável por sustentar os módulos financeiros do Orbiafly. O produto é utilizado para acompanhar contas, cartões, despesas, recebimentos, compras e empréstimos, com atenção à diferença entre **saldo real**, **valores previstos** e **limite de crédito**.

> **Projeto pessoal em evolução contínua.** O Orbiafly não depende de um serviço público centralizado: a aplicação foi pensada para ser executada em infraestrutura própria.

## Repositórios

| Componente | Repositório |
| --- | --- |
| **Backend / API — este repositório** | [orbiafly-back-end](https://github.com/cesarrafaeljunior/orbiafly-back-end) |
| **Frontend Angular** | [orbiafly-front-end](https://github.com/cesarrafaeljunior/orbiafly-front-end) |

Para imagens da aplicação, fluxo de navegação e funcionalidades visíveis ao usuário, consulte o **[README do frontend](https://github.com/cesarrafaeljunior/orbiafly-front-end#readme)**.

## Contexto

A proposta do Orbiafly é reunir o acompanhamento das finanças pessoais sem reduzir todas as operações a uma lista genérica de entradas e saídas. O domínio exige tratar separadamente compromissos recorrentes, gastos eventuais, saldo bancário, crédito, compras parceladas e valores a receber.

A API concentra a lógica necessária para o frontend consultar e registrar essas informações de forma consistente.

## Domínios funcionais

| Domínio | Responsabilidade de negócio |
| --- | --- |
| **Contas** | Dados de contas, acompanhamento de saldos, movimentações, ajustes e transferências. |
| **Cartões** | Cadastro de cartões, limites, situação e datas relacionadas ao ciclo de cobrança. |
| **Compras no cartão** | Compras à vista e parceladas, relacionamento com cartões e acompanhamento por competência. |
| **Despesas fixas** | Controle de obrigações recorrentes, vencimentos, pagamentos e valores reservados. |
| **Gastos avulsos** | Registro e acompanhamento de despesas pontuais. |
| **Recebimentos** | Gestão de valores esperados e confirmação de recebimentos efetivos. |
| **Empréstimos** | Registro e acompanhamento de valores devidos ou a receber e respectivas liquidações. |
| **Usuários e perfil** | Acesso à aplicação, dados de perfil e operações relativas às credenciais. |

Esta tabela descreve **capacidades apresentadas na interface**, não uma relação exaustiva de endpoints, entidades JPA ou serviços internos. A implementação deve ser consultada para nomes e contratos exatos.

## Stack técnica

Tecnologias registradas durante o desenvolvimento do Orbiafly:

- **Java 21**
- **Spring Boot**
- **Maven**
- **PostgreSQL**
- **Flyway** para evolução do esquema do banco
- **Docker / Docker Compose** para execução dos serviços de infraestrutura e/ou aplicação
- **pgAdmin** como ferramenta opcional de administração local do banco

As versões exatas de Spring Boot, drivers e demais dependências devem ser verificadas no `pom.xml` e nos arquivos de configuração em uso.

## Princípios de modelagem

A organização das regras financeiras deve preservar alguns conceitos essenciais:

- **Saldo de conta não é limite de cartão.** Crédito disponível não representa saldo bancário.
- **Previsão não é efetivação.** Uma entrada prevista não deve alterar o saldo real antes do evento de recebimento.
- **Competência e vencimento têm significados diferentes.** Compras e despesas precisam ser apresentadas no período ao qual pertencem.
- **Movimentações financeiras exigem rastreabilidade.** Ajustes, transferências e pagamentos devem ter contexto suficiente para compreender seus efeitos.
- **Contas e cartões inativos exigem tratamento explícito.** O comportamento de consulta, edição e reativação precisa ser consistente com as regras implementadas.

Esses pontos descrevem a intenção funcional do produto e não substituem a documentação detalhada das regras presentes no código.

## Execução local

### Pré-requisitos

- **JDK 21**.
- **Maven**, ou o Maven Wrapper caso o repositório inclua `mvnw`.
- **PostgreSQL** acessível à aplicação.
- **Docker e Docker Compose** caso você utilize a infraestrutura containerizada do projeto.

### 1. Clonar o repositório

```bash
git clone https://github.com/cesarrafaeljunior/orbiafly-back-end.git
cd orbiafly-back-end
```

### 2. Preparar o banco de dados

Configure uma instância PostgreSQL e um banco exclusivo para o Orbiafly. Se o ambiente utiliza Docker Compose, execute o arquivo Compose **no diretório em que ele realmente se encontra**. O nome do arquivo e os serviços devem ser conferidos na instalação atual.

Para um PostgreSQL configurado pelas variáveis padrão da imagem oficial, os parâmetros típicos são:

```dotenv
POSTGRES_DB=orbiafly
POSTGRES_USER=seu_usuario
POSTGRES_PASSWORD=sua_senha_forte
```

Os valores acima são **exemplos**, não credenciais nem nomes de variáveis confirmados do projeto.

### 3. Configurar a conexão da aplicação

Confira os arquivos `application.yml` / `application.properties` e os perfis ativos do Spring Boot. Se a configuração utilizar as propriedades padrão do Spring, o equivalente em variáveis de ambiente será:

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/orbiafly
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha_forte
```

**Atenção:** ajuste host, porta, nome do banco e variáveis conforme a configuração real. Em Docker Compose, o host do banco costuma ser o nome do serviço, e não `localhost`.

Não adicione credenciais reais ao versionamento. Use um arquivo de exemplo sem segredos quando aplicável.

### 4. Iniciar a API

Com o Maven instalado:

```bash
mvn spring-boot:run
```

Se o projeto incluir o Maven Wrapper, também poderá ser utilizado:

```bash
./mvnw spring-boot:run
```

Verifique os perfis Spring, configurações obrigatórias e a porta HTTP efetiva nos arquivos da aplicação. Não há porta ou URL de API presumida nesta documentação.

### 5. Gerar o artefato

```bash
mvn clean package
```

O arquivo gerado seguirá o nome e a configuração de empacotamento definidos no `pom.xml`.

### Migrações do banco

O projeto utiliza **Flyway** para evolução do esquema. Confira as migrações disponíveis e a configuração do perfil ativo antes de iniciar a API com uma base existente. Evite aplicar migrações de desenvolvimento diretamente sobre uma base com dados pessoais sem um backup adequado.

## Integração com o frontend

O [frontend Angular](https://github.com/cesarrafaeljunior/orbiafly-front-end) consome a API para apresentar informações e executar operações financeiras.

Para uma instalação funcional:

1. Inicie o banco de dados.
2. Configure e inicie a API.
3. Configure no frontend a URL base da API.
4. Inicie o frontend e valide as principais operações de consulta e gravação.

Se os serviços estiverem em origens distintas, a configuração de **CORS** precisa permitir a origem usada pelo frontend. Confira o código para conhecer os caminhos reais das rotas, os formatos de autenticação e os contratos de entrada/saída: este README não inventa endpoints.

## Segurança e dados

Por tratar informações financeiras pessoais, recomenda-se atenção especial a:

- Proteção de credenciais, segredos de autenticação e arquivos de ambiente.
- Isolamento dos dados de cada usuário, conforme o modelo de autorização implementado.
- Tratamento transacional das operações que alteram saldo ou situação financeira.
- Validações de entrada e consistência das regras de negócio no servidor.
- Backup e recuperação do PostgreSQL antes de modificações estruturais.
- Revisão do histórico Git antes de tornar este repositório público.

O README não fornece usuários ou senhas padrão nem presume que exista uma instância pública disponível.

## Estado do projeto

O Orbiafly é um produto pessoal utilizado e aprimorado conforme surgem novas necessidades. A documentação funcional pode evoluir junto com os módulos e regras de negócio.

> **Nota de manutenção:** mantenha os comandos, perfis, configurações e exemplos de execução deste documento sincronizados com os arquivos efetivamente utilizados pelo projeto.

## Licenciamento

A visibilidade pública de um repositório não implica uma licença de uso, modificação ou redistribuição. Consulte o arquivo `LICENSE`, se existir, para as condições aplicáveis.

---

**Autor:** [César Rafael](https://github.com/cesarrafaeljunior)  
**Frontend:** [orbiafly-front-end](https://github.com/cesarrafaeljunior/orbiafly-front-end)
