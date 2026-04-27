# PRD: Whatzzi (WhatsApp Marketing Assistant + Order Intake)

## Summary
Whatzzi is a WhatsApp Business chatbot that markets our WhatsApp chatbot solutions, qualifies leads safely, collects orders for a standard "same-version" chatbot delivery, and notifies the owner on WhatsApp when an order is submitted.

This PRD is for the existing Spring Boot + Meta WhatsApp Cloud API webhook bot in this repo.

## Goals
- Provide professional, trustworthy marketing responses about our chatbot offering.
- Qualify leads and reduce risk from fake/malicious clients.
- Collect order details and require explicit confirmation before submission.
- Notify the business owner via WhatsApp when an order is confirmed/submitted.
- Enable a $0 (or near-$0) hosting deployment path suitable for production (hosting cost only).
- Ensure automated unit tests run in CI/CD on every PR/push.

## Non-Goals
- Handling payments inside WhatsApp.
- Multi-tenant "one bot serves many client businesses" (this is for our own marketing bot).
- Supporting unofficial WhatsApp libraries (no WhatsApp Web scraping).

## Personas
- Prospect (Client): a potential customer messaging our WhatsApp number.
- Owner (Admin): the business owner receiving order notifications and delivering the chatbot to the client.

## Confirmed Inputs
- Admin/owner WhatsApp number for submitted-order notifications: `+94717465220`
- Languages: English + Sinhala + Tamil (respond in the user's language when possible)
- Pricing: static package tiers with fixed prices; tiers are configured (not invented by the LLM)
- Required order fields (minimum): client's WhatsApp Business number + client's personal number to receive notifications
- Persistence: store orders in PostgreSQL
- Webhook authenticity: enable Meta webhook signature verification
- Deployment: hosting-only free tier is acceptable
- Response strategy:
  - If message is exactly `hi` (case-insensitive, trimmed) -> return a static reply (no DB, no AI)
  - Else if message matches a known question -> answer from DB
  - Else -> call AI and reply

## Core Bot Instruction (System Prompt Contract)
The chatbot must follow this instruction as non-negotiable policy:

> You are my marketing assistance. Be professional and market our self. Be trustworthy and care about our business safety around fake clients. Get a confirmation before submitting an order.

## Order Definition (v1)
An "order" is a request from a prospect to purchase/deliver our standard WhatsApp chatbot solution.

Minimum required order fields (v1):
- Selected package tier (from a configured catalog with static prices)
- Prospect WhatsApp number (auto from sender)
- Client WhatsApp Business number (provided by prospect)
- Client personal WhatsApp number to receive order notifications (provided by prospect)

Optional order fields (v1):
- Contact name
- Business/company name
- Preferred language
- Notes / requirements (free text)

## User Stories + BDD Acceptance Criteria

### US1: Marketing Assistant Replies
As a Prospect, I want to ask questions about your WhatsApp chatbot solution and get professional answers so that I can decide whether to buy.

Acceptance Criteria:
Given a prospect sends `hi`
When the bot receives the message
Then the bot responds with the configured static greeting message (no DB lookup, no AI call).

Given a prospect sends a message that matches a known question
When the bot receives the message
Then the bot replies with the stored DB answer for that question (no AI call).

Given a prospect sends a general inquiry (features, pricing, timeline, support)
When the bot receives the message
Then the bot responds professionally, markets our solution, and asks one relevant follow-up question.

Given a prospect asks for something unrelated to buying/building chatbots (spam, harassment, unrelated requests)
When the bot receives the message
Then the bot refuses briefly and offers the correct next steps for business inquiries.

Given the bot cannot answer confidently (missing info, ambiguous question)
When the bot receives the message
Then the bot asks clarifying questions instead of inventing details.

Security/Edge Notes:
- The bot must not claim certifications, guarantees, or pricing that we have not configured.
- The bot must not request or reveal secrets (tokens, passwords, API keys).

### US2: Safe Lead Qualification
As the Owner, I want the bot to qualify leads and avoid risky engagements so that we reduce time wasted on fake clients and protect the business.

Acceptance Criteria:
Given a prospect asks to proceed with an order
When the bot starts the order flow
Then the bot collects the minimum required order fields and warns it will require confirmation before submission.

Given a prospect requests suspicious/unsafe actions (e.g., asks for our access tokens, asks for admin credentials, asks for source code dumps)
When the bot receives the message
Then the bot refuses and asks the prospect to provide legitimate business details and a callback time for manual verification.

Given a prospect provides incomplete or conflicting order details
When the bot attempts to compile the order
Then the bot asks only for the missing fields and does not submit an order.

Security/Edge Notes:
- Meta webhook payload signature verification (Meta `X-Hub-Signature-256`) must be supported and enabled in production to prevent spoofed requests.
- Rate-limit per sender to reduce abuse.

### US3: Order Confirmation Before Submission
As a Prospect, I want to see a summary of my order and confirm it before submission so that I can prevent mistakes.

Acceptance Criteria:
Given the prospect has provided all required order fields
When the bot is ready to submit
Then the bot sends an order summary and asks for an explicit confirmation (e.g., "CONFIRM" / "CANCEL").

Given the prospect replies with confirmation
When the bot receives the confirmation
Then the system persists the order with status `SUBMITTED` and sends an acknowledgement to the prospect.

Given the prospect replies with cancellation or refuses to confirm
When the bot receives that response
Then the system does not persist the order as submitted, and the bot offers to edit details or end the flow.

### US4: Owner Notification on Submitted Orders
As the Owner, I want to receive a WhatsApp message on my personal number when an order is submitted so that I can deliver the same chatbot solution to the client.

Acceptance Criteria:
Given an order is persisted with status `SUBMITTED`
When the submission is completed
Then the system sends a WhatsApp message to the configured owner/admin WhatsApp number containing the order summary and prospect contact number.

Given the configured owner number is missing or invalid
When an order is submitted
Then the system logs an error and still confirms submission to the prospect (no crash, no webhook failure).

Given the WhatsApp send-message API returns an error
When sending the owner notification
Then the system retries with backoff (bounded) or records a failure state for later manual follow-up.

### US5: Zero-Cost Production Deployment Path (Hosting Cost Only)
As the Owner, I want a $0 hosting deployment option with a rollback plan so that I can run this in production without paying for hosting.

Acceptance Criteria:
Given the repository contains deployment documentation
When I follow the documented steps on a $0 host (e.g., free-tier VM)
Then the bot can receive webhook traffic over HTTPS and respond correctly.

Given a new release is deployed
When a rollback is needed
Then the documentation includes explicit rollback steps to restore the previous working version.

Failure Paths:
Given environment variables are missing (DB URL, WhatsApp tokens, LLM keys)
When the app starts
Then it fails fast with clear startup errors explaining which config is missing.

Notes:
- This requirement covers hosting cost only; WhatsApp Cloud API and LLM providers may have usage-based fees.

### US6: CI/CD Unit Tests
As the Owner, I want automated unit tests in CI/CD so that changes do not break the bot.

Acceptance Criteria:
Given a pull request or push to main
When CI runs
Then `./mvnw test` runs and the build fails on any test failures.

Given new order-flow logic is added
When tests run
Then unit tests cover happy path and at least two failure paths for order confirmation and admin notification.

## Test Coverage Map (Draft)
- US1: unit tests for prompt/policy enforcement; integration test for `/llm/test-handle-text` behavior.
- US2: unit tests for intent classification + validation + refusal logic; security tests for signature verification (if implemented).
- US3: unit tests for order state machine (collect -> summarize -> confirm/cancel).
- US4: unit tests for admin notification formatting and error handling; integration tests with a mocked WhatsApp send API client.
- US5: smoke test checklist (manual) plus container healthcheck; CI verifies configuration validation.
- US6: GitHub Actions pipeline for Maven tests.

## Security & Ops Requirements
- No secrets in git: all API keys/tokens must be provided via environment variables or secret managers; any previously committed secrets must be rotated before production use.
- Webhook authenticity: verify Meta webhook signatures (`X-Hub-Signature-256`) for all incoming webhook requests (production).
- Logging hygiene: never log access tokens; avoid logging full webhook payloads in production.
- Data retention: define a retention period for stored chat logs and orders (default recommendation: 30-90 days unless legally required longer).
