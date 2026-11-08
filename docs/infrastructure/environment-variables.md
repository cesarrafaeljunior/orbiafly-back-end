# Variaveis de ambiente

`.env.example` documenta as chaves sem fornecer segredos reutilizaveis. `.env` e ignorado pelo Git e nunca deve conter credenciais de producao versionadas. Gere valores unicos para cada ambiente.

## Aplicacao Spring

| Variavel | Default local | Finalidade |
|---|---|---|
| `APP_PORT` | `4000` | Porta HTTP da aplicacao |
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5487` | Porta do PostgreSQL em execucao local |
| `DB_NAME` | `orbiafly` | Nome do banco |
| `DB_USERNAME` | `orbiafly` | Usuario do banco |
| `DB_PASSWORD` | `orbiafly` | Senha usada somente no profile local |
| `APP_CORS_ALLOWED_ORIGINS` | origins locais | Origins CORS separadas por virgula; cookies exigem origins explicitas |
| `APP_TIME_ZONE` | `America/Sao_Paulo` | Fuso usado para derivar a data atual |
| `APP_JWT_SECRET` | apenas no profile local | Segredo de no minimo 32 caracteres; obrigatorio em producao |
| `APP_JWT_ISSUER` | `orbiafly-api` | Emissor criado e validado no JWT |
| `APP_JWT_AUDIENCE` | `orbiafly-web` | Audiencia criada e validada no JWT |
| `APP_JWT_EXPIRATION_MINUTES` | `60` | Duracao maxima da sessao |
| `APP_AUTH_COOKIE_SECURE` | `false` local / `true` prod | Envia o cookie de sessao apenas por HTTPS |
| `APP_AUTH_RATE_LIMIT_ATTEMPTS` | `10` | Tentativas por IP, endpoint e janela |
| `APP_AUTH_RATE_LIMIT_WINDOW_SECONDS` | `60` | Janela da limitacao de autenticacao |
| `APP_ADMIN_EMAIL` | apenas no profile local | E-mail do administrador inicial; obrigatorio em producao |
| `APP_ADMIN_USERNAME` | apenas no profile local | Usuario do administrador inicial; obrigatorio em producao |
| `APP_ADMIN_PASSWORD` | apenas no profile local | Senha forte do administrador inicial; obrigatoria em producao |
| `MAIL_HOST` | `localhost` local | Servidor SMTP usado fora de local/test |
| `MAIL_PORT` | `1025` local / `587` prod | Porta SMTP |
| `MAIL_USERNAME` | sem default | Usuario SMTP |
| `MAIL_PASSWORD` | sem default | Senha SMTP |
| `MAIL_FROM` | `MAIL_USERNAME` | Remetente dos codigos de recuperacao |

No profile `prod`, credenciais de banco, administrador, JWT, CORS e SMTP nao possuem fallback de desenvolvimento. A aplicacao falha na inicializacao quando uma variavel obrigatoria esta ausente.

## Docker Compose

| Variavel | Default local | Finalidade |
|---|---|---|
| `POSTGRES_DB` | `orbiafly` | Banco criado pelo container |
| `POSTGRES_USER` | `orbiafly` | Usuario criado pelo container |
| `POSTGRES_PASSWORD` | obrigatoria | Senha do container |
| `POSTGRES_PORT` | `5487` | Porta publicada do banco |
| `PGADMIN_DEFAULT_EMAIL` | `admin@orbiafly.com` | Login local do pgAdmin |
| `PGADMIN_DEFAULT_PASSWORD` | obrigatoria | Senha local do pgAdmin |
| `PGADMIN_PORT` | `5050` | Porta publicada do pgAdmin |

No profile `full`, o Compose converte `POSTGRES_*` em `DB_*` para o backend. Os segredos e dados do administrador tambem sao obrigatorios; o Compose nao fornece mais credenciais previsiveis.
