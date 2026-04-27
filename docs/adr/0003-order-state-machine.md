# ADR 0003: Explicit Order State Machine (Deterministic)

## Context
Order intake must be safe and require explicit confirmation before submission. Using free-form LLM reasoning for state tracking risks inconsistent outcomes.

## Options Considered
1. Deterministic state machine (stored state per prospect).
2. LLM-only "extract and infer" approach with prompt instructions.
3. Hybrid: LLM assists wording, deterministic state persists fields.

## Decision
Use a deterministic order state machine with persisted state/fields; optionally use LLM only for natural language phrasing.

## Rationale
- Guarantees confirmation gating (no accidental submission).
- Easier to test with unit tests and CI.
- Reduces prompt injection risk affecting order submission.

## Consequences
- Pros: predictable behavior, higher safety, testability.
- Cons: more implementation effort than LLM-only.

