# ADR 0002: PostgreSQL for Orders and Chat Logs

## Context
We need durable storage for chat logs and confirmed orders, with query capability for operations and support.

## Options Considered
1. PostgreSQL (JPA).
2. File-based storage.
3. No persistence (notify-only).

## Decision
Use PostgreSQL for both chat logs and orders.

## Rationale
- Matches existing repo setup (JPA + Postgres).
- Durable storage with flexible querying.
- Enables future reporting and admin tooling.

## Consequences
- Pros: reliability, queryability, future extensibility.
- Cons: requires DB availability/maintenance (especially on free-tier VM).

