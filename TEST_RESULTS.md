# Test Results Report

- **Date:** 2026-10-06
- **Suite:** `store.OrderCalculatorTest` (JUnit Jupiter 5.10.2)
- **Runner:** `junit-platform-console-standalone-1.10.2.jar` (bundled in `lib/`)
- **JDK:** OpenJDK 11
- **Command:** `./test.sh`
- **Result:** **15 tests found, 15 successful, 0 failed** (exit code 0)

## Captured output

```
.
+-- JUnit Jupiter [OK]
| '-- OrderCalculatorTest [OK]
|   +-- deliveryFeeFollowsZone() [OK]
|   +-- totalIsDiscountedPlusDeliveryPlusTax() [OK]
|   +-- promoCodeGivesTenPercent() [OK]
|   +-- freeDeliveryStartsExactlyAtThreshold() [OK]
|   +-- tieredDiscountUsesInclusiveThresholds() [OK]
|   +-- combinedDiscountsAreCappedAt25Percent() [OK]
|   +-- taxIsSevenPointFivePercentOfDiscountedAmount() [OK]
|   +-- badZoneAndCustomerTypeInputsAreRejected() [OK]
|   +-- emptyOrderIsRejected() [OK]
|   +-- negativePriceIsRejected() [OK]
|   +-- premiumCustomerGetsFivePercent() [OK]
|   +-- zeroAndHugeQuantitiesAreRejected() [OK]
|   +-- subtotalAddsUpLineTotals() [OK]
|   +-- freeShipPromoWaivesDelivery() [OK]
|   '-- unknownPromoIsIgnoredWithNote() [OK]
+-- JUnit Vintage [OK]
'-- JUnit Platform Suite [OK]

Test run finished after 159 ms
[         4 containers found      ]
[         0 containers skipped    ]
[         4 containers started    ]
[         0 containers aborted    ]
[         4 containers successful ]
[         0 containers failed     ]
[        15 tests found           ]
[         0 tests skipped         ]
[        15 tests started         ]
[         0 tests aborted         ]
[        15 tests successful      ]
[         0 tests failed          ]
```

\* exact method name in the source is `taxIsSevenPointFivePercentOfDiscountedAmount()`.

## Coverage of the required case types

| Case type   | Tests |
|-------------|-------|
| Normal      | `subtotalAddsUpLineTotals`, `promoCodeGivesTenPercent`, `premiumCustomerGetsFivePercent`, `taxIsSevenPointFivePercentOfDiscountedAmount`, `totalIsDiscountedPlusDeliveryPlusTax`, `deliveryFeeFollowsZone`, `freeShipPromoWaivesDelivery` |
| Boundary    | `tieredDiscountUsesInclusiveThresholds` (49,999.99 / 50,000.00 / 99,999.99 / 100,000.00), `freeDeliveryStartsExactlyAtThreshold` (149,999.99 / 150,000.00), `combinedDiscountsAreCappedAt25Percent` (35% -> 25% cap) |
| Invalid     | `negativePriceIsRejected`, `zeroAndHugeQuantitiesAreRejected`, `emptyOrderIsRejected`, `unknownPromoIsIgnoredWithNote`, `badZoneAndCustomerTypeInputsAreRejected` |

Re-run any time with `./test.sh`; the same tree and counters are printed to the console.
