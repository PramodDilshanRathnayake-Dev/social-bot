# FAQ Entries (Known Questions)

Known questions are stored in PostgreSQL table `faq_entries`.

The router uses a normalized key: lowercase, trimmed, and internal whitespace collapsed.

Example insert (PostgreSQL):
```sql
insert into faq_entries (question_key, language_code, answer_body, enabled, created_at, updated_at)
values ('what is the price', 'en', 'Our packages are BASIC / ... (static tiers). Reply ORDER to place an order.', true, now(), now());
```

