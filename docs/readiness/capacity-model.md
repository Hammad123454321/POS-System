# Capacity Model

Approved initial sizing assumptions:

- up to 10 POS devices per store
- up to 200 open restaurant orders or tabs per store at peak
- up to 50 payment attempts per minute per busy store
- up to 10,000 sync events per device per day
- up to 1,000 active stores per region before the next capacity review

Implementation implications for Phase 1a:

- keys and indexes are designed around `merchant_id`, `store_id`, and `business_date`
- sync and idempotency tables are append-heavy and indexed for high write throughput
- queue, reporting, and hot data caches remain isolated by concern from day one

