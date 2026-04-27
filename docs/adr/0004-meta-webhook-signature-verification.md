# ADR 0004: Meta Webhook Signature Verification (X-Hub-Signature-256)

## Context
Webhook endpoints can be attacked with spoofed payloads unless authenticity is verified. The PRD requires signature verification in production.

## Options Considered
1. Verify `X-Hub-Signature-256` using HMAC-SHA256 with Meta App Secret.
2. Rely only on obscurity and a verify token.
3. IP allowlisting only.

## Decision
Implement `X-Hub-Signature-256` verification using Meta App Secret.

## Rationale
- Standard and recommended by Meta for webhook integrity/authenticity.
- Strong protection against spoofed requests.

## Consequences
- Pros: improved security posture.
- Cons: requires safely managing Meta App Secret and careful canonicalization of request bytes.

