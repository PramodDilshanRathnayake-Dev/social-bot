# System Architecture Document (SAD): Whatzzi

## Scope
This SAD covers the WhatsApp marketing-assistant chatbot ("Whatzzi") implemented as a Spring Boot application receiving Meta WhatsApp Cloud API webhooks and sending WhatsApp messages through the Meta Graph API.

It implements:
- Marketing assistant responses (LLM-backed, policy constrained)
- Order intake with explicit confirmation before submission
- PostgreSQL persistence for chat logs and orders
- Admin (owner) notification via WhatsApp on order submission
- Meta webhook signature verification (production)

## High-Level Architecture
Single deployable Spring Boot service (monolith) with PostgreSQL.

```
                     +------------------------------+
                     |        Meta WhatsApp         |
                     |   Cloud API / Graph API      |
                     +--------------+---------------+
                                    |
                                    | HTTPS webhook (GET verify, POST events)
                                    v
                         +----------+-----------+
                         |   Spring Boot App    |
                         |  (Webhook Handler)   |
                         +----------+-----------+
                                    |
                 +------------------+------------------+
                 |                                     |
                 v                                     v
       +---------+----------+                 +--------+---------+
       |  Order Orchestrator|                 |   LLM Responder  |
       |  (state machine)   |                 | (Spring AI Chat) |
       +---------+----------+                 +--------+---------+
                 |                                     |
                 +------------------+------------------+
                                    |
                                    v
                              +-----+------+
                              | PostgreSQL |
                              +------------+
                                    |
                                    | Meta send-message API (outbound)
                                    v
                           +--------+---------+
                           | WhatsApp Notifier|
                           | (prospect/admin) |
                           +------------------+
```

## Components
- Webhook Controller
  - `GET /webhook`: Meta verification challenge.
  - `POST /webhook`: receives inbound WhatsApp events (messages).
- Webhook Signature Verifier
  - Validates `X-Hub-Signature-256` for inbound webhook requests using the Meta App Secret.
  - On failure: returns `401 Unauthorized` (or `403 Forbidden`) and does not process the payload.
- Message Router (Deterministic Strategy)
  - Extracts sender (`from`), message type, and text content.
  - Applies the response strategy in order:
    - If message is exactly `hi` (case-insensitive, trimmed): static reply
    - Else if message matches a known question: DB answer
    - Else: LLM responder
  - Routes to Order Orchestrator when the conversation is in an active order flow (state machine overrides general marketing routing).
- FAQ Resolver (Known Questions)
  - Normalizes inbound text and looks up an answer in PostgreSQL.
  - Returns a deterministic stored answer when matched.
- Order Orchestrator (State Machine)
  - Manages order intake: collect required fields, present summary, request explicit confirmation, persist submission.
  - Uses deterministic templates for summaries and confirmations (LLM is not the source of truth for order fields).
- LLM Responder
  - Uses Spring AI `ChatClient` with a system policy to keep responses professional and business-safe.
  - Must not invent pricing; it can only present package tiers from the configured catalog.
- WhatsApp Notifier (Outbound)
  - Sends WhatsApp messages back to the prospect and to the configured admin number via Meta Graph API.
- Persistence
  - `chat_logs` table (existing): inbound/outbound message history.
  - `orders` table (new): order records with status and required fields.

## API Contracts

### 1) Webhook Verification
`GET /webhook`

Query parameters:
- `hub.mode` (string)
- `hub.verify_token` (string)
- `hub.challenge` (string)

Responses:
- `200 OK` with body `hub.challenge` when verification succeeds.
- `403 Forbidden` otherwise.

### 2) Webhook Receiver
`POST /webhook`

Headers:
- `Content-Type: application/json`
- `X-Hub-Signature-256: sha256=<hex>` (required in production)

Request body:
- Meta WhatsApp Cloud API webhook payload (JSON). The implementation extracts:
  - `entry[].changes[].value.messages[]`
  - `messages[].from` (sender phone)
  - `messages[].type`
  - `messages[].text.body` for text messages

Responses:
- `200 OK` always for valid webhook requests (Meta expects quick acknowledgement).
- `401/403` if signature verification is enabled and fails.

Error handling:
- Any processing failure must not crash the request thread; errors are logged and `200 OK` is still returned after best-effort handling (unless signature verification fails).

### 3) Test Endpoint (Non-Production)
`POST /llm/test-handle-text`

Request JSON:
```json
{
  "from": "api-test",
  "message": "hello",
  "sendToWhatsapp": false
}
```

Response JSON (200):
```json
{
  "from": "api-test",
  "message": "hello",
  "reply": "…",
  "sendToWhatsapp": false,
  "error": null
}
```

Responses:
- `400 Bad Request` if `message` is missing/blank.

## Data Model

### Table: chat_logs (existing)
Purpose: store inbound/outbound conversation history.

Fields:
- `id` BIGSERIAL PRIMARY KEY
- `app_user_phone` TEXT NOT NULL
- `message_direction` TEXT NOT NULL  (values: `INCOMING`, `OUTGOING`)
- `message_body` VARCHAR(2000)
- `timestamp` TIMESTAMP NOT NULL

Indexes:
- `idx_chat_logs_user_phone_ts` on (`app_user_phone`, `timestamp`)

### Table: orders (new)
Purpose: persist order intake and submission.

Fields:
- `id` BIGSERIAL PRIMARY KEY
- `status` TEXT NOT NULL
  - Allowed values: `DRAFT`, `AWAITING_CONFIRMATION`, `SUBMITTED`, `CANCELLED`
- `step` TEXT NOT NULL
  - Allowed values: `SELECT_PACKAGE`, `ASK_CLIENT_BUSINESS_NUMBER`, `ASK_CLIENT_PERSONAL_NOTIFY_NUMBER`, `CONFIRM`
- `prospect_phone` TEXT NOT NULL
- `package_code` TEXT NULL (required before confirmation)
- `package_name` TEXT NULL (required before confirmation)
- `package_price_minor` BIGINT NULL (required before confirmation)
  - "minor" units (e.g. cents); interpretation depends on `currency`
- `currency` CHAR(3) NULL (required before confirmation)
- `client_whatsapp_business_number` TEXT NULL (required before confirmation)
- `client_personal_notify_number` TEXT NULL (required before confirmation)
- `language_code` TEXT NOT NULL
  - Allowed values: `en`, `si`, `ta`
- `notes` TEXT NULL
- `created_at` TIMESTAMP NOT NULL
- `updated_at` TIMESTAMP NOT NULL
- `submitted_at` TIMESTAMP NULL

Constraints:
- `status` must be one of the allowed values.
- `step` must be one of the allowed values.
- `currency` must be a 3-letter ISO code when present (enforced in app validation).

Indexes:
- `idx_orders_prospect_phone_created_at` on (`prospect_phone`, `created_at`)
- `idx_orders_status_created_at` on (`status`, `created_at`)

### Table: faq_entries (new)
Purpose: store deterministic "known question" answers.

Fields:
- `id` BIGSERIAL PRIMARY KEY
- `question_key` TEXT NOT NULL
  - normalized key used for matching (e.g. lowercase trimmed text)
- `language_code` TEXT NOT NULL
  - Allowed values: `en`, `si`, `ta`
- `answer_body` TEXT NOT NULL
- `enabled` BOOLEAN NOT NULL
- `created_at` TIMESTAMP NOT NULL
- `updated_at` TIMESTAMP NOT NULL

Constraints:
- unique index on (`question_key`, `language_code`)

Indexes:
- `idx_faq_entries_question_key` on (`question_key`)

## Package Catalog (Static Pricing)
Package tiers are static (fixed price) and must be configured (not invented by the LLM).

Configuration shape (conceptual):
- `whatzzi.packages[]`
  - `code` (string, stable identifier)
  - `name` (string)
  - `priceMinor` (integer)
  - `currency` (string, ISO 4217)
  - `description` (string)

At runtime, the bot:
- Lists tiers and prices from this catalog.
- Requires the prospect to select one tier before confirming an order.

## Security
- Webhook authenticity:
  - Verify Meta signature header `X-Hub-Signature-256` using HMAC-SHA256 with the Meta App Secret.
- Secrets handling:
  - Never store secrets in git.
  - Use environment variables for all tokens/keys.
- Logging:
  - Do not log access tokens.
  - In production, avoid logging full webhook payloads; log minimal structured fields (sender, message id, message type).
- Abuse controls:
  - Per-sender rate limiting (basic).
  - Refusal policy for suspicious requests (token exfiltration, credential requests, etc.).

## Failure Modes and Resilience
- Meta send-message API fails:
  - Log the failure and optionally retry with bounded backoff; do not block webhook ack.
- Database unavailable:
  - Fail fast at startup for hard dependency failures (production).
  - During runtime, if DB writes fail, log and return a safe response; do not crash the webhook handler.
- LLM provider unavailable:
  - Return a fallback message offering manual follow-up and continue order collection using deterministic prompts.

## Deployment Topology (Free-Tier Friendly)
- Single container or single JVM process.
- HTTPS termination:
  - Either platform-managed TLS or a reverse proxy on a free-tier VM.
- PostgreSQL:
  - Preferred: managed Postgres (may not be $0).
  - Near-$0 option: self-host Postgres on the same free-tier VM (with backups).

## Environment Variable Manifest (Names Only)
- WhatsApp / Meta:
  - `WHATSAPP_VERIFY_TOKEN`
  - `WHATSAPP_ACCESS_TOKEN`
  - `WHATSAPP_PHONE_NUMBER_ID`
  - `WHATSAPP_META_API_URL`
  - `WHATSAPP_META_APP_SECRET`
  - `WHATSAPP_META_SIGNATURE_VERIFICATION_ENABLED`
- Whatzzi:
  - `WHATZZI_ADMIN_NUMBER`
  - `WHATZZI_HI_REPLY`
  - `WHATZZI_PACKAGES_0_CODE` (and similar indexed vars for package tiers)
- LLM:
  - `SPRING_AI_OPENAI_API_KEY`
  - `SPRING_AI_OPENAI_BASE_URL`
  - `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL`
- Database:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
- App:
  - `PORT`

## Observability
- Logs:
  - Inbound message receipt (sanitized), order state transitions, outbound message attempts.
- Metrics (Actuator):
  - HTTP request counts/latency, error counts, DB connectivity health.
- Health:
  - `/actuator/health` for liveness/readiness checks (secured as appropriate).
