# ADR 0001: Spring Boot Monolith for Whatzzi

## Context
Whatzzi needs to receive WhatsApp webhooks, call an LLM, persist data to PostgreSQL, and send outbound WhatsApp messages. The scope is small, and operational simplicity is a primary goal.

## Options Considered
1. Single Spring Boot service (monolith).
2. Split services (webhook gateway, order service, notifier, etc.).

## Decision
Use a single Spring Boot monolith.

## Rationale
- Fewer moving parts, faster iteration, simpler deployment on free-tier hosting.
- Lower operational burden (single deployment artifact).
- Adequate for current scope and expected traffic.

## Consequences
- Pros: simpler debugging and deployment; lower cost.
- Cons: less isolation between concerns; future scaling might require refactoring.

