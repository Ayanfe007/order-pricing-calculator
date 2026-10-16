# Week-03 Test Results

**Environment:** OpenJDK 17.0.20.1 · JUnit Jupiter 5.10.2 · clean build via `./build.sh`

## Current run

```text
./build.sh  -> Build OK.
./test.sh   -> 41 tests found, 41 successful, 0 failed
```

| Suite | Cases | Coverage |
|---|---:|---|
| `store.service.OrderPricingServiceTest` | 15 | Preserved Week-01 subtotal, promo, member/tier discounts, VAT, totals, delivery, caps, validation, and input parsing rules |
| `store.model.DomainModelTest` | 16 | Preserved Week-02 domain checks plus two tests that prevent partial order/delivery state transitions |
| `store.service.OrderPricingPoliciesTest` | 5 | Tiered/promo policies, cap composition, zone/free/threshold shipping, legacy total |
| `store.service.PaymentProcessingServiceTest` | 3 | Card and transfer implementations, unsupported-method rejection without state mutation |
| `store.service.OrderApplicationServiceTest` | 2 | Repository-backed order lifecycle, stock state, and missing product handling |
| **Total** | **41** | **41 passed · 0 failed** |

## Regression evidence

- The original Week-01/02 set contained **29 tests** (15 pricing + 14 domain); every original scenario/assertion remains represented in the renamed/moved suites.
- Twelve Week-03 checks were added: five pricing/shipping policy tests, three payment-strategy tests, two application/repository tests, and two atomic lifecycle failure tests.
- `samples/sample1.txt` still prints **NGN 31,740.00**.
- Catalog run with 2 Jollof Party Packs, 4 Zobo drinks, premium customer, WELCOME10, Zone B also prints **NGN 31,740.00**.

The build now removes `out/` and `out-test/` before compiling, preventing stale `.class` files from being included in test discovery.
