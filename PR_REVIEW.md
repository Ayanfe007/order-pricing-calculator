# Week-03 PR Review Record

**Status:** local pre-PR review completed; GitHub pull request not yet opened. This file records maintainer review findings and implemented corrections; it is **not** fabricated GitHub reviewer activity. A second person must leave the actual review on GitHub after the branch is pushed.

- **Target branch:** `main`
- **Working branch:** `refactor/week-03-maintainable`
- **Suggested PR title:** `Refactor e-commerce order application into maintainable packages`
- **Suggested label:** `week-03`

## Review comments and corrections

### Comment 1 — Keep the domain model independent of application services

> The Week-02 `Order` currently translates itself into the Week-01 pricing request and directly calls the calculator. That couples the aggregate to a service and makes it harder to test or replace pricing.

**Correction applied:** removed `priceBreakdown(...)` from `model.Order`. `OrderPricingService.calculate(Order, promoCode)` now performs the adapter mapping from product-backed lines to `PricingOrder` in the service layer.

**Evidence:** `src/main/java/store/model/Order.java`, `src/main/java/store/service/OrderPricingService.java`, `DomainModelTest.priceBreakdownReusesWeek1Rules`.

### Comment 2 — Avoid partial state changes when a child transition fails

> `Order.ship()` and `Order.deliver()` changed the aggregate status before asking the `Delivery` state machine to transition. If delivery refused the operation, the order could be left in a state that contradicts its delivery.

**Correction applied:** transition delivery first; commit the order status and loyalty side effect only after the delivery move succeeds.

**Evidence:** `Order.ship()` / `Order.deliver()` in `model.Order`; tests `failedDeliveryStartDoesNotPartiallyAdvanceOrderStatus` and `failedDeliveryCompletionDoesNotPartiallyDeliverOrder`.

### Comment 3 — Do not duplicate receipt formatting across front ends

> `ConsoleApp` and `CatalogApp` each printed the same totals, tax, delivery, notes, and currency formatting. Fixing one could leave the other inconsistent.

**Correction applied:** both front ends use `app.OrderSummaryPrinter`; shared parsing and re-prompting is in `app.ConsoleInput`.

**Evidence:** `src/main/java/store/app/OrderSummaryPrinter.java`, `ConsoleInput.java`; both app paths were manually run.

### Comment 4 — Make pricing rule alternatives testable and replaceable

> A single calculator with hard-coded branches is difficult to extend. Separate the discount, shipping, and payment decisions behind small contracts and compose the Week-02 rules.

**Correction applied:** introduced `DiscountPolicy`, `ShippingCalculator`, and `PaymentProcessor`; the default service composes tier, promo, customer, zone, free-shipping, and threshold strategies. Card and bank-transfer processors are independently selected by `PaymentProcessingService`.

**Evidence:** `OrderPricingPoliciesTest` (5), `PaymentProcessingServiceTest` (3), and `OrderPricingServiceTest` regression cases.

### Comment 5 — Persist every product mutation at the repository boundary

> Cancellation returns reserved stock in the aggregate. Make sure an application service using a repository saves each changed product, rather than relying on in-memory object identity.

**Correction applied:** `OrderApplicationService.cancel()` saves each product after the aggregate restores stock; adding a product also saves the stock change.

**Evidence:** `OrderApplicationServiceTest.serviceCoordinatesRepositoriesPaymentAndLifecycle` and `DomainModelTest.addItemReservesStockAndCancelReturnsIt`.

### Comment 6 — Prevent stale compiled tests from masking the real count

> The build did not clean `out-test`, so renamed/moved test classes could remain discoverable and be counted twice.

**Correction applied:** `build.sh` removes `out/` and `out-test/` before compiling.

**Evidence:** a clean run reports exactly **41 tests**, not the stale-class count.

## Verification at review time

```text
./build.sh  -> Build OK (OpenJDK 17)
./test.sh   -> 41 tests found, 41 successful, 0 failed
sample1     -> NGN 31,740.00
catalog run -> NGN 31,740.00
```

## Suggested PR description

> Refactors the Week-02 e-commerce application into `app`, `model`, `service`, `repository`, `validation`, `exception`, and `util` packages. Adds composable discount/shipping/payment strategies and repository boundaries, removes pricing-service coupling from the order aggregate, centralizes receipt/input behavior, and preserves the previous pricing and lifecycle rules. Adds regression and implementation tests. Includes before/after package diagram, review checklist, and this local review record.
>
> **Tests:** `./build.sh && ./test.sh` — 41/41 passing on JDK 17.
