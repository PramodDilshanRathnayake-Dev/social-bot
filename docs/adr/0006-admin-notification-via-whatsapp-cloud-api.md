# ADR 0006: Admin Notification via WhatsApp Cloud API

## Context
Owner must receive a WhatsApp message on their personal number when an order is submitted.

## Options Considered
1. Send admin notifications using WhatsApp Cloud API (same outbound channel).
2. Send email/SMS notifications instead.
3. Rely on DB polling/manual checks.

## Decision
Send admin notifications via WhatsApp Cloud API to a configured admin number.

## Rationale
- Matches the primary channel and owner workflow.
- Immediate notification without additional services.

## Consequences
- Pros: fast, simple, consistent.
- Cons: depends on Meta send-message API availability; requires correct number formatting.

