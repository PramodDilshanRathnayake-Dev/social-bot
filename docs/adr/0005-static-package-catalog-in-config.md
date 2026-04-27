# ADR 0005: Static Package Catalog in App Configuration

## Context
Pricing must be static and consistent. The bot must not invent pricing, and the owner should be able to set tiers without LLM drift.

## Options Considered
1. Hardcode tiers in code.
2. Store tiers in application configuration (YAML/properties).
3. Store tiers in DB with an admin UI.

## Decision
Store tiers in application configuration.

## Rationale
- Simple and free-tier friendly (no additional admin UI).
- Ensures deterministic price listing and order summaries.
- Updating tiers requires only config change + redeploy, not code changes.

## Consequences
- Pros: simple, safe, testable.
- Cons: changing prices requires redeploy; no runtime editing UI.

