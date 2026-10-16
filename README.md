# Maintainable E-commerce Order Application

Week-03 refactor of the Week-01/02 order-pricing project. The same NGN checkout rules and Week-02 order lifecycle are retained, but responsibilities are separated into packages and interchangeable strategies.

## Run it

Requires **JDK 17 or newer**. JUnit 5's standalone launcher is bundled in `lib/`.

```bash
./build.sh      # clean compile of application and tests
./test.sh       # 41 tests: 29 Week-01/02 regression cases + 12 new refactor cases
./run.sh        # interactive Week-01 pricing console
java -cp out store.app.CatalogApp       # catalog/stock front end
java -cp out store.app.DomainDemo       # Week-02 lifecycle demo
```

The clean build removes `out/` and `out-test/` before compilation so stale class files cannot make an old test appear to pass.

## Package structure

```text
src/main/java/store/
├── app/          ConsoleInput, ConsoleApp, CatalogApp, DomainDemo, OrderSummaryPrinter
├── model/        Customer, Product, Order, Payment, Delivery, value records/enums,
│                 plus PricingOrder/PricingLineItem and PriceBreakdown
├── service/      Application/pricing services and strategy interfaces/implementations
├── repository/   Repository interfaces and in-memory implementations
├── validation/  Reusable model-construction/input validation
├── exception/    Typed validation, state, stock, not-found, and payment exceptions
└── util/         Money rounding, pricing configuration, and shared formatting
```

The **before/after package diagram** is [`docs/package-diagram-before-after.svg`](docs/package-diagram-before-after.svg). The Week-02 class diagram remains available at [`docs/uml-class-diagram.svg`](docs/uml-class-diagram.svg) as a historical domain-model view.

### Deliberate model split

- `model.Order` is the Week-02 **lifecycle aggregate**: it owns product-backed order lines, stock reservation/return, payment and delivery transitions, refunds, and loyalty points.
- `model.PricingOrder` is a validated **pricing request** used by the Week-01 calculator. `model.PricingLineItem` holds a name, price, and quantity for that calculation.
- `OrderPricingService.calculate(Order, promoCode)` adapts the lifecycle aggregate to a pricing request. This keeps the model independent of pricing services and avoids a confusing pair of classes both called `Order` in the same package.

## Interfaces and composition

| Interface | Implementations | Responsibility |
|---|---|---|
| `DiscountPolicy` | `TieredDiscountPolicy`, `PromoCodeDiscountPolicy`, `CustomerTypeDiscountPolicy`, `CompositeDiscountPolicy` | Calculate one discount source; compose the sources and apply the 25% cap once |
| `ShippingCalculator` | `ZoneShippingCalculator`, `FreeShippingCalculator`, `ThresholdShippingCalculator` | Calculate a zone fee; compose the free-shipping promo/threshold around it |
| `PaymentProcessor` | `CardPaymentProcessor`, `BankTransferPaymentProcessor` | Simulate settlement for the supported payment method; the router chooses an implementation |
| `ProductRepository` | `InMemoryProductRepository` | Product lookup, list, and save boundary |
| `OrderRepository` | `InMemoryOrderRepository` | Order lookup and save boundary |

Services receive collaborators through constructors and compose strategies instead of inheriting behavior. Payment processors are **local demonstrations only**: they mark a payment paid and do not contact a card network or bank.

## Main refactoring decisions

1. **Separate data, use cases, and I/O.** Domain classes no longer parse input, print receipts, or construct pricing-service dependencies. The pricing service is pure; `OrderApplicationService` coordinates repositories and the payment-processing service.
2. **Move validation to named validators.** `AddressValidator`, `CustomerValidator`, `ProductValidator`, `PaymentValidator`, `OrderValidator`, and `OrderItemValidator` keep validation rules out of long constructors. Typed exceptions extend `IllegalArgumentException`/`IllegalStateException` so existing callers can still catch the old broad types.
3. **Make alternatives swappable.** Discount, shipping, and payment behavior sit behind interfaces; composite/decorator-style services use composition. Two payment processors and multiple discount/shipping strategies are exercised in unit tests.
4. **Remove duplicated presentation logic.** `OrderSummaryPrinter` formats the receipt for both console front ends. `ConsoleInput` centralizes shared parsing and re-prompt behavior. Large workflow methods were split into named steps.
5. **Avoid partial lifecycle updates.** `Order.ship()` starts delivery before changing order status, and `Order.deliver()` completes delivery before changing order status or awarding points. Regression tests cover the failure paths.
6. **Preserve NGN rules and outputs.** Tiers remain inclusive at ₦50,000 and ₦100,000; WELCOME10/VIP20/FREESHIP, the 25% cap, the free-delivery threshold, 7.5% VAT on discounted goods only, and the guarded Week-02 lifecycle remain intact.

## Verification

- Baseline before refactor: **29/29** original Week-01/02 tests passed.
- After refactor: **41/41** tests pass (all original cases retained, plus 12 tests for policy composition, shipping/payment implementations, repositories/application service, and atomic lifecycle failures).
- `samples/sample1.txt` still prices at **NGN 31,740.00**.
- Catalog scenario (Jollof Party Pack ×2 + Zobo ×4, premium, WELCOME10, Zone B) still totals **NGN 31,740.00**.
- See [`TEST_RESULTS.md`](TEST_RESULTS.md), [`CODE_REVIEW_CHECKLIST.md`](CODE_REVIEW_CHECKLIST.md), and [`PR_REVIEW.md`](PR_REVIEW.md) for detailed evidence and review notes.

## GitHub release

The repository is [Ayanfe007/order-pricing-calculator](https://github.com/Ayanfe007/order-pricing-calculator). Week-03 work is prepared on branch `refactor/week-03-maintainable`; publish that branch, open a pull request into `main`, and push the annotated `week-03` tag after final review. The local review record is in `PR_REVIEW.md`; it is not a substitute for a real GitHub review approval.
