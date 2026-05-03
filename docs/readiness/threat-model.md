# STRIDE Threat Model Scope

The approved Phase 1 threat model covers:

- device theft and offline queue exposure
- rogue cashier and insider misuse
- compromised merchant back-office account
- tenant isolation failure
- payment replay and duplicate submission
- webhook spoofing on future integrations
- refresh-token theft and reuse

Phase 1 implementation requirements derived from this model:

- device-local state remains encrypted at rest
- all mutating POS endpoints require idempotency
- tenant-scoped data paths carry explicit merchant context
- archived access and security exceptions remain audit-visible

