# Online Store - Order Pricing & Delivery Calculator

Week-01 assignment: a **console-based Java application** for an online store
that calculates an order's **subtotal, discount, delivery fee, tax and final
amount**.

The business logic (`OrderCalculator`, `PricingConfig`, `Order`, `OrderItem`,
`DeliveryZone`, `CustomerType`, `PriceBreakdown`) is completely separate from
the console input/output class (`ConsoleApp`), so every pricing rule is
unit-testable without a keyboard - and it is tested: **15 JUnit 5 tests**
covering normal, invalid and boundary cases (see `TEST_RESULTS.md`).

## Requirements

- JDK 11 or newer (developed and verified on OpenJDK 11)
- No build tool needed - the JUnit console launcher jar is bundled in `lib/`

## Setup, build, run, test

```bash
./build.sh     # compiles src/main/java -> out/ and src/test/java -> out-test/
./run.sh       # starts the console app (reads from the terminal)
./test.sh      # runs the 15 JUnit tests
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
|   `-- ConsoleApp.java            console front-end (input + printing only)
|-- src/test/java/store/OrderCalculatorTest.java   15 JUnit 5 tests
|-- README.md                      this file
|-- TEST_RESULTS.md                test-results report (15/15 passing)
|-- SAMPLE_OUTPUTS.md              sample inputs + captured outputs
`-- DEBUGGING_NOTES.md             personal debugging notes
```

## Tests

`./test.sh` - expects **15 tests, 0 failures**. The full captured report,
including which tests cover normal / boundary / invalid cases, is in
[`TEST_RESULTS.md`](TEST_RESULTS.md). Debugging history is in
[`DEBUGGING_NOTES.md`](DEBUGGING_NOTES.md).

## Submitting to GitHub

The local repository already has an incremental commit history and the
annotated tag `week-01` on the final commit. To publish:

```bash
# on github.com: create an empty repository, then:
git remote add origin https://github.com/<your-username>/<repo-name>.git
git push -u origin main
git push origin week-01
```
