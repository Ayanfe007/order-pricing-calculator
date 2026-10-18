# Code Review Checklist — Week 03

Scope: local code review of `refactor/week-03-maintainable` against the Week-02 baseline. The checklist is about the code and evidence in this branch; it is **not** a claim that a GitHub PR or external approval has happened.

## Design and structure

- [x] Application entry points are isolated in `store.app`.
- [x] Domain and pricing request objects are in `store.model`; `Order` is distinct from `PricingOrder`.
- [x] Use cases and business policies are in `store.service`.
- [x] Repository contracts are in `store.repository`; in-memory adapters implement them.
- [x] Validation rules and typed exceptions are in `store.validation` and `store.exception`.
- [x] Shared rounding, formatting, and rule configuration are in `store.util`.
- [x] At least two useful interfaces have more than one implementation: `DiscountPolicy`, `ShippingCalculator`, and `PaymentProcessor`.
- [x] Strategies are composed/injected; business alternatives do not depend on a deep inheritance hierarchy.
- [x] The `model` package no longer imports pricing or application services.
- [x] Pricing rules remain pure (no `Scanner`, console printing, repository, or network access).

## Correctness and maintainability

- [x] All 29 Week-01/02 regression cases remain represented in the test suite.
- [x] Added tests exercise discount composition/cap, zone/free/threshold shipping, card/transfer routing, unsupported payment methods, repository-backed order flow, and atomic lifecycle failure behavior.
- [x] `Order.ship()` and `Order.deliver()` only commit the aggregate status after the dependent delivery transition succeeds.
- [x] Receipt formatting is shared by both console front ends; common parsing/re-prompt logic is shared in `ConsoleInput`.
- [x] Stock reservation and return remain encapsulated in the aggregate/product model; service tests verify saved state.
- [x] Existing broad exception catches remain compatible because typed validation/state exceptions subclass `IllegalArgumentException`/`IllegalStateException`.
- [x] Validation errors, unsupported payment method, and missing repository entities have clear exception types/messages.
- [x] `build.sh` cleans output directories so stale compiled test classes cannot inflate test counts.

## Evidence

- [x] OpenJDK 17 clean build: `./build.sh` — Build OK.
- [x] Test run: `./test.sh` — **41 found, 41 successful, 0 failed**.
- [x] Week-01 sample input `samples/sample1.txt` — total remains NGN 31,740.00.
- [x] Catalog regression scenario (2 Jollof Party Packs + 4 Zobo, premium, WELCOME10, Zone B) — total remains NGN 31,740.00.
- [x] `docs/package-diagram-before-after.svg` documents the structural change.
- [x] `README.md` explains the principal design decisions.
- [x] `PR_REVIEW.md` records comments and corrections applied during local review.

## GitHub release gate (not yet completed from this workspace)

- [ ] Push branch and tag, open the pull request, and obtain an actual external reviewer comment/approval. The workspace has no authenticated GitHub session/connector, so this step must be completed from an authenticated Git client. Do not represent the local review record as GitHub review history.
