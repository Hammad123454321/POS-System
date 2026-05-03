# Performance Budgets

Phase 1 budgets that implementation must protect:

- POS cold start to interactive shell: under 3 seconds on supported store hardware
- add-to-cart local interaction: under 100 ms perceived latency
- cash checkout confirmation: under 300 ms once request reaches the API
- receipt payload generation: under 150 ms server-side
- sync event enqueue on device: under 50 ms local write latency

These budgets are used as implementation guardrails and will be validated as the corresponding flows are delivered.

