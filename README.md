# social-bot

Spring Boot WhatsApp webhook chatbot that uses Spring AI (OpenAI-compatible) to generate replies and stores message history in PostgreSQL.

## What It Does

- Exposes a Meta WhatsApp webhook at `GET /webhook` (verification) and `POST /webhook` (incoming messages).
- For incoming text messages, generates a response via Spring AI `ChatClient` and sends it back through the WhatsApp Cloud API.
- Persists incoming/outgoing messages to PostgreSQL table `chat_logs`.
- Implements a deterministic routing strategy:
  - `hi` -> static reply
  - known question -> answer from DB (`faq_entries`)
  - otherwise -> AI reply
- Supports an order flow with explicit confirmation and admin notification on submission.

## Prerequisites

- Java 21 (the project is configured for `java.version=21` in `pom.xml`)
- Optional: Docker (to run PostgreSQL via `docker-compose.yml`)

## Configuration

This project reads configuration from Spring properties and environment variables.

### Example Config File

See `application.example.yml` for a safe template (no secrets) showing all required settings.

### Sample `application.properties`

Use this as a starting point for local development (keep secrets in env vars, not in git):

```properties
spring.application.name=social-bot

# Server
server.port=${PORT:8080}

# Database (example: docker-compose Postgres on localhost:5433)
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/bot_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
spring.jpa.show-sql=${SPRING_JPA_SHOW_SQL:false}

# Spring AI (OpenAI-compatible)
spring.ai.openai.api-key=${SPRING_AI_OPENAI_API_KEY:}
spring.ai.openai.base-url=${SPRING_AI_OPENAI_BASE_URL:https://api.groq.com/openai}
spring.ai.openai.chat.options.model=${SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL:llama-3.3-70b-versatile}

# WhatsApp Meta API Settings
whatsapp.meta.verify-token=${WHATSAPP_VERIFY_TOKEN:my_secure_token}
whatsapp.meta.access-token=${WHATSAPP_ACCESS_TOKEN:}
whatsapp.meta.phone-number-id=${WHATSAPP_PHONE_NUMBER_ID:}
whatsapp.meta.api-url=${WHATSAPP_META_API_URL:https://graph.facebook.com/v25.0}

# Optional: enable webhook signature verification (recommended for production)
whatsapp.meta.app-secret=${WHATSAPP_META_APP_SECRET:}
whatsapp.meta.signature-verification-enabled=${WHATSAPP_META_SIGNATURE_VERIFICATION_ENABLED:false}
```

### Database

You can run Postgres locally, or start the included container:

```bash
docker compose up -d
```

`docker-compose.yml` exposes Postgres on `localhost:5433` with:

- database: `bot_db`
- user: `postgres`
- password: `postgres`

To use the container from the app, set:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/bot_db"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
```

### LLM (Spring AI)

The app uses Spring AI's OpenAI-compatible client (`spring-ai-openai-spring-boot-starter`). Configure it with:

```bash
export SPRING_AI_OPENAI_API_KEY="..."
export SPRING_AI_OPENAI_BASE_URL="https://api.groq.com/openai"   # optional
export SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL="llama-3.3-70b-versatile"  # optional
```

Notes:

- If you use OpenAI directly, you typically do not need to set `SPRING_AI_OPENAI_BASE_URL`.
- Any OpenAI-compatible provider should work if it supports the `/chat/completions` API shape Spring AI expects.

### WhatsApp Cloud API (Meta)

Required settings:

```bash
export WHATSAPP_VERIFY_TOKEN="my_secure_token"
export WHATSAPP_ACCESS_TOKEN="..."
export WHATSAPP_PHONE_NUMBER_ID="..."
export WHATSAPP_META_API_URL="https://graph.facebook.com/v25.0"
```

These map to Spring properties under `whatsapp.meta.*` (see `src/main/java/com/socialbot/social_bot/config/WhatsAppConfig.java`).

## Run

```bash
./mvnw spring-boot:run
```

The app listens on port `8080` by default.

## Test

```bash
./mvnw test
```

## CI

GitHub Actions runs `./mvnw test` on pushes and pull requests (see `.github/workflows/ci.yml`).

## Webhook Endpoints

- Verification (Meta calls this during webhook setup):
  - `GET /webhook?hub.mode=subscribe&hub.verify_token=...&hub.challenge=...`
- Incoming messages:
  - `POST /webhook` (Meta sends JSON payloads here)

For local development, you typically need a public tunnel (for example `ngrok http 8080`) and then configure the callback URL in Meta to `https://<tunnel-host>/webhook`.

## Security Notes

- Do not commit API keys or access tokens. Use environment variables and keep secrets out of git.
- This repo already ignores `.env` and `src/main/resources/application.properties` via `.gitignore`.
