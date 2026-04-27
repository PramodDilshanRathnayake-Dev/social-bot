# ADR 0007: Deterministic Response Routing (hi -> static, FAQ -> DB, else -> AI)

## Context
We want to control cost, reduce AI calls for common questions, and ensure predictable responses for greetings and known FAQs.

The owner-approved strategy is:
1. If inbound message is exactly `hi` (case-insensitive, trimmed): return a static reply.
2. Else if the message matches a known question: reply from DB.
3. Else: call the LLM and reply.

## Options Considered
1. Always call AI for every message.
2. Rule-based routing + DB FAQ + AI fallback (this ADR).
3. Vector search / embeddings retrieval for FAQ matching.

## Decision
Implement rule-based routing with a DB FAQ resolver and AI fallback.

## Rationale
- Low-cost for common interactions (no AI call for greetings/known questions).
- Deterministic behavior for frequently asked questions and pricing guidance.
- Testable with unit tests.

## Consequences
- Pros: cheaper, more predictable, safer against hallucinations for known topics.
- Cons: "known question" matching is limited unless we later add synonyms/embeddings; requires managing FAQ entries.

