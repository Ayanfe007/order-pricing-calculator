package store;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 tests for the pricing rules: normal cases, invalid input and
 * boundary values (exactly-at-threshold amounts).
 */
class OrderCalculatorTest {

    private static final double D = 0.001; // delta for money comparisons
    private final OrderCalculator calc = new OrderCalculator();

    private static Order singleItemOrder(double amount, String promo,
                                         CustomerType type, DeliveryZone zone) {
        return new Order(List.of(new OrderItem("Test item", amount, 1)),
                promo, type, zone);
    }

    // ---------------- normal cases ----------------

    @Test
    void subtotalAddsUpLineTotals() {
        Order order = new Order(Arrays.asList(
                new OrderItem("Rice", 2500, 2),
                new OrderItem("Beans", 10000, 1)),
                "", CustomerType.STANDARD, DeliveryZone.ZONE_A);
        assertEquals(15000.0, calc.subtotal(order), D);
    }

    @Test
    void promoCodeGivesTenPercent() {
        Order order = singleItemOrder(10000, "WELCOME10",
                CustomerType.STANDARD, DeliveryZone.ZONE_A);
        PriceBreakdown bd = calc.calculate(order);
        assertEquals(1000.0, bd.discountAmount(), D);
        assertEquals(9000.0, bd.discountedSubtotal(), D);
    }

    @Test
    void premiumCustomerGetsFivePercent() {
        Order order = singleItemOrder(10000, "", CustomerType.PREMIUM, DeliveryZone.ZONE_A);
        assertEquals(500.0, calc.calculate(order).discountAmount(), D);
    }

    @Test
    void taxIsSevenPointFivePercentOfDiscountedAmount() {
        assertEquals(750.0, calc.tax(10000), D);
    }

    @Test
    void totalIsDiscountedPlusDeliveryPlusTax() {
        Order order = singleItemOrder(32000, "WELCOME10",
                CustomerType.PREMIUM, DeliveryZone.ZONE_B);
        PriceBreakdown bd = calc.calculate(order);
        // 15% of 32,000 = 4,800 -> discounted 27,200; fee 2,500; VAT 2,040
        assertEquals(31740.0, bd.total(), D);
    }

    @Test
    void deliveryFeeFollowsZone() {
        Order order = singleItemOrder(1000, "", CustomerType.STANDARD, DeliveryZone.ZONE_C);
        PriceBreakdown bd = calc.calculate(order);
        assertEquals(5000.0, bd.deliveryFee(), D);
    }

    @Test
    void freeShipPromoWaivesDelivery() {
        Order order = singleItemOrder(1000, "FREESHIP",
                CustomerType.STANDARD, DeliveryZone.ZONE_C);
        PriceBreakdown bd = calc.calculate(order);
        assertEquals(0.0, bd.deliveryFee(), D);
        assertTrue(bd.notes().stream().anyMatch(n -> n.contains("FREESHIP")));
    }

    // ---------------- boundary cases ----------------

    @Test
    void tieredDiscountUsesInclusiveThresholds() {
        assertEquals(0.0,  calc.tieredRate(49999.99), D);
        assertEquals(0.05, calc.tieredRate(50000.00), D);
        assertEquals(0.05, calc.tieredRate(99999.99), D);
        assertEquals(0.10, calc.tieredRate(100000.00), D);
    }

    @Test
    void freeDeliveryStartsExactlyAtThreshold() {
        Order order = singleItemOrder(1000, "", CustomerType.STANDARD, DeliveryZone.ZONE_B);
        assertTrue(calc.deliveryWaived(order, 150000.00));
        assertFalse(calc.deliveryWaived(order, 149999.99));
    }

    @Test
    void combinedDiscountsAreCappedAt25Percent() {
        // 10% (tier) + 20% (VIP20) + 5% (premium) = 35% -> capped at 25%
        Order order = singleItemOrder(100000, "VIP20",
                CustomerType.PREMIUM, DeliveryZone.ZONE_A);
        PriceBreakdown bd = calc.calculate(order);
        assertEquals(0.25, bd.discountRate(), D);
        assertEquals(25000.0, bd.discountAmount(), D);
    }

    // ---------------- invalid cases ----------------

    @Test
    void negativePriceIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("Rice", -5, 1));
        assertTrue(e.getMessage().contains("positive"));
    }

    @Test
    void zeroAndHugeQuantitiesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("Rice", 100, 0));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("Rice", 100, 101));
    }

    @Test
    void emptyOrderIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order(List.of(), "", CustomerType.STANDARD, DeliveryZone.ZONE_A));
    }

    @Test
    void unknownPromoIsIgnoredWithNote() {
        Order order = singleItemOrder(10000, "BOGUS99",
                CustomerType.STANDARD, DeliveryZone.ZONE_A);
        PriceBreakdown bd = calc.calculate(order);
        assertEquals(0.0, bd.discountAmount(), D);
        assertTrue(bd.notes().stream().anyMatch(n -> n.contains("not recognised")));
    }

    @Test
    void badZoneAndCustomerTypeInputsAreRejected() {
        IllegalArgumentException zone = assertThrows(IllegalArgumentException.class,
                () -> DeliveryZone.fromInput("X"));
        assertTrue(zone.getMessage().contains("Valid zones"));
        assertThrows(IllegalArgumentException.class, () -> CustomerType.fromInput("gold"));
    }
}
