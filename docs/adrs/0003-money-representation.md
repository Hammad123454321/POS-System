# ADR 0003: Integer Minor Units For Money

Status: accepted

## Decision

Store and transmit all money values as integer minor units plus ISO currency code, and use a dedicated money utility layer in Laravel.

## Rationale

- The platform handles tax, receipts, stored value, refunds, and register variance.
- Floating-point arithmetic is not acceptable for financial correctness.
- The approved plan requires deterministic rounding and reconciliation at minor-unit precision.

## Consequences

- Database columns use integer types for money.
- API payloads expose minor units, not decimals.
- Money math is centralized so rounding rules stay consistent across modules.

