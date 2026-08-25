# Variaveis de ambiente

`.env.example` contem somente valores locais de exemplo. `.env` e ignorado pelo Git e nunca deve conter credenciais de producao versionadas.

## Aplicacao Spring

| Variavel | Default local | Finalidade |
|---|---|---|
| `APP_PORT` | `8080` | Porta HTTP da aplicacao |
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `orbiafly` | Nome do banco |
| `DB_USERNAME` | `orbiafly` | Usuario do banco |
| `DB_PASSWORD` | `change_me` | Senha local do banco |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origins CORS separadas por virgula |
| `APP_TIME_ZONE` | `America/Sao_Paulo` | Fuso usado para derivar a data atual e despesas vencidas |

## Docker Compose

| Variavel | Default local | Finalidade |
|---|---|---|
| `POSTGRES_DB` | `orbiafly` | Banco criado pelo container |
| `POSTGRES_USER` | `orbiafly` | Usuario criado pelo container |
| `POSTGRES_PASSWORD` | `change_me` | Senha local do container |
| `POSTGRES_PORT` | `5432` | Porta publicada do banco |
| `PGADMIN_DEFAULT_EMAIL` | `admin@orbiafly.com` | Login ficticio local do pgAdmin |
| `PGADMIN_DEFAULT_PASSWORD` | `change_me` | Senha local do pgAdmin |
| `PGADMIN_PORT` | `5050` | Porta publicada do pgAdmin |

No profile `full`, o Compose converte `POSTGRES_*` em `DB_*` para o container do backend. Ao executar Spring na maquina, mantenha os defaults alinhados ou exporte as variaveis `DB_*` correspondentes.
