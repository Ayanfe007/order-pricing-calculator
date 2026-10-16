# Design-Decision Notes — Weeks 2 and 3

These notes explain the Week-02 domain model and the Week-03 package/service refactor. The Week-02 class diagram is preserved at `docs/uml-class-diagram.svg`; the Week-03 package comparison is `docs/package-diagram-before-after.svg`.

## 1. Classes, records, and enums

The selection rule is: **does this thing have identity and changing state, or is it an immutable value / fixed vocabulary?**

| Kind | Types | Reason |
|---|---|---|
| Class | `Customer`, `Product`, `Order`, `Payment`, `Delivery` | Mutable identity with guarded behavior: loyalty points, stock, order/payment/delivery lifecycle |
| Record | `Address`, domain `OrderItem` | Immutable value/snapshot; domain `OrderItem` is a product + quantity pair with derived `lineTotal()` |
| Enum | `CustomerType`, `DeliveryZone`, `OrderStatus`, `PaymentStatus`, `DeliveryStatus` | Small fixed sets of customer categories, locations, and states |

`model.Order` is the aggregate root: it owns item reservation, confirmation, shipment, delivery, cancellation, refunds, and loyalty effects. Status enums name states; transition logic remains beside the objects whose payment/delivery conditions it needs.

## 2. Keep the aggregate independent from pricing

There are intentionally two request shapes:

- `model.Order` is the mutable Week-02 e-commerce aggregate, with `Product`-backed lines and lifecycle state.
- `model.PricingOrder` is the immutable Week-01 pricing request; `PricingLineItem` contains only display name, unit price, and quantity.

`OrderPricingService.calculate(Order, promoCode)` maps the aggregate to the pricing request in the **service layer**. The model does not construct or call a pricing service. This avoids duplicating price rules and avoids coupling a domain entity to an application use case.

## 3. Package boundaries and dependency direction

- `app` owns console input, re-prompting, entry points, and receipt formatting.
- `service` owns pricing, checkout orchestration, and strategy selection.
- `repository` defines persistence boundaries and provides in-memory adapters.
- `model` owns domain state and immutable values.
- `validation` centralizes object-construction/input rules; `exception` names common failure categories.
- `util` owns shared price rounding, formatting, and pricing-rule constants.

The flow is **app → service → model/repository/util**. The model does not depend on services. Validators use primitive/value inputs and throw typed exceptions, avoiding a model/validator import cycle.

## 4. Interfaces and composition

`DiscountPolicy` separates tier, promo, and customer discounts. `CompositeDiscountPolicy` composes them and applies the shared 25% cap exactly once. `ShippingCalculator` has zone, free, and threshold/composed implementations. `PaymentProcessor` has card and bank-transfer implementations; `PaymentProcessingService` routes to a supporting processor. `ProductRepository` and `OrderRepository` can be replaced by durable adapters without changing application-service code.

These are strategy boundaries, not a class hierarchy of business variants. Services receive collaborators through constructors and compose them. Payment processor implementations are local simulations; they do not contact real financial networks.

## 5. Validation and errors

- `AddressValidator`, `CustomerValidator`, `ProductValidator`, `PaymentValidator`, `OrderValidator`, and `OrderItemValidator` hold named input/invariant rules.
- `ValidationException` is an `IllegalArgumentException`; `StateTransitionException` is an `IllegalStateException`. `InsufficientStockException` and `UnsupportedPaymentMethodException` specialize those categories. Existing callers catching the broad Java exceptions remain compatible.
- `NotFoundException` is used by application services when a repository lookup fails.

## 6. Lifecycle side effects and atomicity

`Order.addItem` reserves stock. `Order.cancel` returns it and refunds a paid payment. Delivery moves alongside order moves, and a successful delivery awards loyalty points. A review found that changing the aggregate status before the child delivery state could leave partial state if the child refused a transition. The order now completes the delivery transition first; new tests pin both failed-shipping and failed-delivery cases.

## 7. Presentation, testability, and remaining trade-offs

`OrderSummaryPrinter` is shared by both console front ends; `ConsoleInput` centralizes repeated parsing/re-prompt behavior. `OrderPricingService` has no console, repository, or network I/O. Tests cover strategies, repositories, lifecycle invariants, and previous pricing boundary cases.

The project still uses `double` plus two-decimal rounding for a teaching exercise; a production payment system should use a currency-safe decimal type and real provider adapters with idempotency, retries, and reconciliation. In-memory repositories intentionally do not persist across process restarts.
