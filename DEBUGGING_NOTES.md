# Personal Debugging Notes

Honest log of what broke while building this calculator and how I fixed it.

## 1. Scanner left-newline bug (2026-10-04)

**Symptom:** in the first draft I mixed `Scanner.nextInt()` / `nextDouble()`
with `nextLine()`. After reading the quantity, the "leftover" newline was
consumed by the next `nextLine()`, so the item-name prompt silently received an
empty string and validation failed on the wrong field.

**Fix:** read *everything* with `nextLine()` and parse the text myself
(`app.ConsoleInput.readParsed`, `readInt`, `readPositiveAmount`). One single reading
strategy means no hidden newline state.

**Lesson:** never mix token-based and line-based Scanner reads.

## 2. Off-by-one at the discount thresholds (2026-10-04)

**Symptom:** I originally wrote `subtotal > TIER1_THRESHOLD`. A customer paying
exactly NGN 50,000.00 got 0% instead of 5%.

**How I found it:** wrote the boundary test
`tieredDiscountUsesInclusiveThresholds` *before* trusting the code; it failed
on the 50,000.00 case.

**Fix:** changed `>` to `>=` in `TieredDiscountPolicy.rateFor`. The test now pins
the inclusive behaviour at 49,999.99 / 50,000.00 / 99,999.99 / 100,000.00.

## 3. Stacked discounts got out of hand (2026-10-05)

**Symptom:** VIP20 (20%) + order-value tier (10%) + premium member (5%) =
35% off. That is not a discount, that is a charity.

**Fix:** added `PricingConfig.MAX_DISCOUNT_RATE = 0.25` and capped the combined
rate with `Math.min(...)` in `CompositeDiscountPolicy`. When the cap kicks
in the breakdown prints "Combined discounts capped at 25%." so the customer can
see why. Covered by `combinedDiscountsAreCappedAt25Percent`.

## 4. Floating point drift in money maths (2026-10-05)

**Symptom:** `10000 * 0.1` produced `1000.0000000000001` in a debug print, and
an exact `assertEquals` on a computed total failed by a fraction of a kobo.

**Fix (two parts):**
1. every monetary value passes through `util.Money.round2`
   (`Math.round(v * 100.0) / 100.0`) before it is stored or added;
2. tests compare money with a delta of `0.001` instead of exact equality.

**Lesson:** `double` is fine for a week-1 exercise *if* you round at the
boundary and never compare raw products exactly.

## 5. Piped input crashed with a stack trace (2026-10-05)

**Symptom:** running `java -cp out store.app.ConsoleApp < samples/sample1.txt` with
a truncated file threw `NoSuchElementException` from `Scanner.nextLine` and a
scary stack trace - terrible UX for the sample-output runs.

**Fix:** `main` catches `NoSuchElementException` and prints
"Error: input ended before the order was complete." instead.

## 6. Tests would not compile: NoClassDefFoundError (2026-10-06)

**Symptom:** first `javac` of the test source failed with
`package org.junit.jupiter.api does not exist`; after a half-fix the run failed
with `NoClassDefFoundError: org/junit/jupiter/api/Test`.

**Fix:** the JUnit console-standalone jar must be on the classpath for *both*
compiling and running the tests. `build.sh` and `test.sh` now include
`lib/junit-platform-console-standalone-1.10.2.jar` in `-cp` / `--class-path`.

## 7. Small launcher warning (2026-10-06)

**Symptom:** the console launcher printed
"WARNING: Delegated to the 'execute' command..." - deprecated implicit mode.

**Fix:** `test.sh` now calls the `execute` command explicitly; output is clean.

---

## 8. Records do not compile on JDK 11 (2026-10-07)

**Symptom:** the Week-2 brief requires records, but the default toolchain here
is OpenJDK 11 - `javac` rejects the `record` keyword outright.

**Fix:** switched compiling and testing to Temurin OpenJDK 17 (downloaded as a
tarball since there was no package-manager access); README now states
JDK 17+. Week-1 code compiles unchanged on 17.

**Lesson:** check the language level a brief implies (records = Java 16+)
before choosing the toolchain.

## 9. One exception type was not enough (2026-10-07)

**Symptom:** the first domain draft threw `IllegalArgumentException` for
everything, so tests could not distinguish "you gave me bad data" from "you
asked at the wrong moment".

**Fix:** constructors delegate to named validators that throw
`ValidationException` (an `IllegalArgumentException`) for bad data;
guarded behaviour methods throw `StateTransitionException` (an
`IllegalStateException`) for bad timing. Tests assert each kind separately.

---

Overall takeaway: writing the boundary tests early caught two real logic bugs
(items 2 and 3). That is the point of keeping the business logic out of the
console classes - `OrderPricingService` can be tested without ever touching a
keyboard.


## Week-03 refactor addendum (2026-10-16)

- Moved entry points to `store.app`, state/value types to `store.model`, and pricing/checkout use cases to `store.service`; see the before/after package diagram.
- Replaced the monolithic calculator with `DiscountPolicy`, `ShippingCalculator`, and `PaymentProcessor` strategies composed by services. Regression tests retain the Week-01/02 cases.
- A clean build initially rediscovered old `.class` files after moving test classes. `build.sh` now deletes `out/` and `out-test/` before compiling.
- Code review also found potential partial order state if a child delivery transition failed. `Order` now updates its status only after delivery succeeds; two failure-path tests pin that correction.
- `OrderSummaryPrinter` removes duplicate receipt formatting between `ConsoleApp` and `CatalogApp`; `ConsoleInput` centralizes common parse/re-prompt behavior.
