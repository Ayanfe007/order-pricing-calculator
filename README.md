# Online Store - Order Pricing & Delivery Calculator

Week-01 assignment: a **console-based Java application** for an online store
that calculates an order's **subtotal, discount, delivery fee, tax and final
amount**.

The business logic (`OrderCalculator`, `PricingConfig`, `Order`, `OrderItem`,
`DeliveryZone`, `CustomerType`, `PriceBreakdown`) is completely separate from
the console input/output class (`ConsoleApp`), so every pricing rule is
unit-testable without a keyboard - and it is tested: **15 JUnit 5 tests**
covering normal, invalid and boundary cases (see `TEST_RESULTS.md`).

**Week-02 extension:** a new `store.domain` package models the e-commerce
order domain - `Customer`, `Product`, `Order`, `OrderItem`, `Address`,
`Payment`, `Delivery` plus `OrderStatus` / `PaymentStatus` / `DeliveryStatus`
enums - with encapsulated state-change validation and a bridge that re-uses
the Week-1 pricing engine. See "Week 2 - domain model" below,
[`DESIGN_NOTES.md`](DESIGN_NOTES.md) and
[`docs/uml-class-diagram.svg`](docs/uml-class-diagram.svg). The full suite is
now **29 tests** (15 pricing + 14 domain).

## Requirements

- JDK 17 or newer (the Week-2 domain package uses records; verified on OpenJDK 17)
- No build tool needed - the JUnit console launcher jar is bundled in `lib/`

## Setup, build, run, test

```bash
./build.sh     # compiles src/main/java -> out/ and src/test/java -> out-test/
./run.sh       # starts the console app (reads from the terminal)
./test.sh      # runs the 29 JUnit tests (15 pricing + 14 domain)
```

Equivalent manual commands:

```bash
mkdir -p out out-test
javac -encoding UTF-8 -d out $(find src/main/java -name '*.java')
javac -encoding UTF-8 -cp "out:lib/junit-platform-console-standalone-1.10.2.jar" \
      -d out-test $(find src/test/java -name '*.java')

java -cp out store.ConsoleApp                      # interactive
java -cp out store.ConsoleApp < samples/sample1.txt   # piped input

java -jar lib/junit-platform-console-standalone-1.10.2.jar execute \
     --class-path "out:out-test" --scan-classpath  # tests
```

## Usage

The app asks, in order: number of items; for each item its name, unit price
and quantity; customer type (`standard` / `premium` / `student`, blank =
standard); promo code (blank = none); delivery zone (`A` / `B` / `C`).
Invalid answers print a clear `Error: ...` message and re-prompt. At the end
it prints the subtotal, discount, discounted subtotal, delivery fee, VAT and
**TOTAL PAYABLE**, plus a list of the rules that fired.

Four ready-made runs (inputs *and* captured outputs, with hand-checks of the
maths) are in [`SAMPLE_OUTPUTS.md`](SAMPLE_OUTPUTS.md):

| Sample | Demonstrates | Total |
|--------|--------------|-------:|
| `samples/sample1.txt` | promo + premium discounts, Zone B fee | NGN 31,740.00 |
| `samples/sample2.txt` | tier + promo + premium capped at 25%, Zone C fee | NGN 150,125.00 |
| `samples/sample3.txt` | 10% tier + free delivery over threshold | NGN 164,475.00 |
| `samples/sample4.txt` | invalid inputs rejected & re-prompted, unknown promo ignored | NGN 53,100.00 |

## Business rules (all constants live in `PricingConfig`)

**Pricing / discount rules (three-plus, as required):**

| # | Rule | Detail |
|---|------|--------|
| 1 | Order-value tiers | subtotal >= 50,000 -> 5%; subtotal >= 100,000 -> 10% (thresholds inclusive) |
| 2 | Promo codes | `WELCOME10` = 10%; `VIP20` = 20%; `FREESHIP` = delivery waived; unknown codes ignored with a message |
| 3 | Customer type | `premium` = 5%; `student` = 5%; `standard` = 0% |
| 4 | Discount cap | combined percentage discounts never exceed 25% |

**Delivery rules (location *and* order value):**

| Zone | Base fee |
|------|---------:|
| A - Lagos Island | NGN 1,500.00 |
| B - Lagos Mainland | NGN 2,500.00 |
| C - Outside Lagos | NGN 5,000.00 |

Free delivery when the discounted subtotal >= NGN 150,000.00 or with
`FREESHIP`.

**Tax:** 7.5% VAT on the discounted goods amount (the delivery fee is not
taxed). All money amounts are rounded to 2 decimal places.

**Final amount:** `total = (subtotal - discount) + deliveryFee + vat`.

**Validation / clear messages:** item name non-empty; unit price must be a
positive number; quantity a whole number 1-100; 1-50 items per order; order
must have at least one item; customer type and zone must be recognised -
every violation produces a specific `Error:` message (console) or
`IllegalArgumentException` (logic layer, used by the tests).

## Project structure

```
.
|-- build.sh, run.sh, test.sh      convenience scripts (JDK only, no Maven)
|-- lib/                           bundled JUnit console-standalone jar
|-- samples/                       sample1.txt .. sample4.txt (piped inputs)
|-- src/main/java/store/
|   |-- PricingConfig.java         every business-rule constant, one place
|   |-- OrderCalculator.java       pure pricing logic (no I/O)
|   |-- Order.java / OrderItem.java        validated input model
|   |-- DeliveryZone.java / CustomerType.java  enums + input parsing
|   |-- PriceBreakdown.java        immutable result object
|   |-- ConsoleApp.java            console front-end (input + printing only)
|   `-- domain/                    Week-2 domain model package
|       |-- Customer.java  Product.java  Order.java  Payment.java  Delivery.java  (classes)
|       |-- Address.java  OrderItem.java (records)
|       |-- OrderStatus.java  PaymentStatus.java  DeliveryStatus.java (enums)
|       `-- DomainDemo.java        lifecycle demo (java -cp out store.domain.DomainDemo)
|-- src/test/java/store/OrderCalculatorTest.java   15 pricing tests
|-- src/test/java/store/domain/DomainModelTest.java 14 domain tests
|-- docs/uml-class-diagram.svg     Week-2 UML class diagram
|-- DESIGN_NOTES.md                design-decision notes (class vs record vs enum)
|-- README.md                      this file
|-- TEST_RESULTS.md                test-results report (29/29 passing)
|-- SAMPLE_OUTPUTS.md              sample inputs + captured outputs
`-- DEBUGGING_NOTES.md             personal debugging notes
```

## Week 2 - domain model (`store.domain`)

The model translates the product requirements into Java objects. Selection
rule: **classes** for entities with identity and changing state, **records**
for immutable value snapshots, **enums** for fixed state vocabularies.

| Type | Kind | Changing state / behaviour |
|------|------|----------------------------|
| `Customer` | class | loyalty points; email/phone/address validation |
| `Product` | class | stock, guarded `decreaseStock`/`increaseStock` (no oversell) |
| `Order` | class (aggregate root) | lifecycle `PENDING -> CONFIRMED -> SHIPPED -> DELIVERED`, or `-> CANCELLED`; every move validated |
| `Payment` | class | `PENDING -> PAID` (retryable from `FAILED`), `PAID -> REFUNDED` |
| `Delivery` | class | `PREPARING -> IN_TRANSIT -> DELIVERED/FAILED`; fee from its address zone |
| `Address` | record | immutable; compact-constructor validation |
| `OrderItem` | record | immutable (product, quantity) line with `lineTotal()` |
| `OrderStatus`, `PaymentStatus`, `DeliveryStatus` | enums | fixed states; transitions guarded in the owning classes |

**Relationships:** Customer 1—* Order; Order 1◆—* OrderItem; OrderItem *—1
Product; Order 1—0..1 Payment; Order 1—0..1 Delivery; Delivery *—1 Address;
Customer 1—1 Address. The model re-uses Week-1 types on purpose:
`CustomerType` and `DeliveryZone` on `Customer`/`Address`, and
`Order.priceBreakdown()` delegates to `OrderCalculator` so pricing rules stay
in one place. Side effects travel with transitions: `addItem` reserves stock,
`cancel` returns it and refunds a paid payment, `deliver` awards loyalty
points. See [`docs/uml-class-diagram.svg`](docs/uml-class-diagram.svg) and
[`DESIGN_NOTES.md`](DESIGN_NOTES.md).

**Demo:** `java -cp out store.domain.DomainDemo` builds a customer, products,
order, payment and delivery, shows a guarded refusal ("Cannot ship when order
ORD-2026-0001 is PENDING"), then walks the happy path to DELIVERED.

## Tests

`./test.sh` - expects **29 tests, 0 failures** (15 pricing + 14 domain). The
captured report is in [`TEST_RESULTS.md`](TEST_RESULTS.md); debugging history
in [`DEBUGGING_NOTES.md`](DEBUGGING_NOTES.md).

## Submitting to GitHub

The local repository already has an incremental commit history and the
annotated tags `week-01` (Week-1 state) and `week-02` (Week-2 state) on the
corresponding commits. To publish:

```bash
# on github.com: create an empty repository, then:
git remote add origin https://github.com/<your-username>/<repo-name>.git
git push -u origin main
git push origin week-01
git push origin week-02
```
