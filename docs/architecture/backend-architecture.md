# Arquitetura do backend

O Orbiafly inicia como monolito modular: uma unica aplicacao Spring Boot e um unico processo de deploy, com limites internos organizados por dominio.

## Organizacao

- `modules`: features de negocio, criadas somente quando entrarem no escopo.
- `infrastructure`: configuracoes e adaptadores tecnicos compartilhados pela aplicacao.
- `shared`: elementos realmente compartilhados e estaveis; nao deve virar deposito generico.

Um modulo futuro podera separar `domain`, `application` e `infrastructure` quando essa divisao reduzir complexidade real. Clean Architecture, arquitetura hexagonal e DDD nao serao aplicados de forma cerimonial.

## Limites

- Controllers tratam transporte HTTP e delegam casos de uso.
- Regras de negocio nao ficam em controllers.
- Entidades JPA nao sao expostas diretamente na API.
- DTOs de API nao sao usados indiscriminadamente como entidades.
- Um modulo nao acessa repositories de outro modulo arbitrariamente.
- Dependencias circulares, services gigantes e classes `Utils` genericas devem ser evitados.
- Persistencia nao deve determinar todo o modelo de dominio.

Microservicos e infraestrutura distribuida nao fazem parte da arquitetura atual.

## Modulo de despesas fixas

O primeiro modulo fica em `modules/fixedexpense` e separa:

- `domain`: entidades e status de pagamento;
- `application`: casos de uso e representacoes retornadas pela API;
- `infrastructure/persistence`: repositories Spring Data;
- `web`: controllers e validacao dos contratos HTTP.

O modulo usa um service de aplicacao como fronteira transacional. Controllers nao acessam repositories e entidades JPA nao sao expostas pela API.
