package store.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import store.model.CustomerType;
import store.model.DeliveryZone;
import store.model.PricingLineItem;
import store.model.PricingOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderPricingPoliciesTest {
    private static PricingOrder order(double price, String promo, CustomerType customer, DeliveryZone zone) {
        return new PricingOrder(List.of(new PricingLineItem("Rice", price, 1)), promo, customer, zone);
    }

    @Test
    void tieredPolicyKeepsThresholdsInclusive() {
        TieredDiscountPolicy policy = new TieredDiscountPolicy();
        assertEquals(0.05, policy.rateFor(50_000), 0.0001);
        assertEquals(0.10, policy.rateFor(100_000), 0.0001);
        assertEquals(0.0, policy.rateFor(49_999.99), 0.0001);
    }

    @Test
    void promoPolicyKeepsPromoAndUnknownCodeMessages() {
        PromoCodeDiscountPolicy policy = new PromoCodeDiscountPolicy();
        DiscountQuote welcome = policy.evaluate(
                order(1_000, "WELCOME10", CustomerType.STANDARD, DeliveryZone.ZONE_A), 1_000);
        assertEquals(0.10, welcome.rate(), 0.0001);
        assertTrue(welcome.notes().get(0).contains("WELCOME10"));
        DiscountQuote unknown = policy.evaluate(
                order(1_000, "BOGUS", CustomerType.STANDARD, DeliveryZone.ZONE_A), 1_000);
        assertEquals(0.0, unknown.rate(), 0.0001);
        assertTrue(unknown.notes().get(0).contains("not recognised"));
    }

    @Test
    void composedPoliciesRespectTheSharedDiscountCap() {
        DiscountPolicy composite = new CompositeDiscountPolicy(List.of(
                new TieredDiscountPolicy(), new PromoCodeDiscountPolicy(),
                new CustomerTypeDiscountPolicy()), 0.25);
        DiscountQuote quote = composite.evaluate(
                order(100_000, "VIP20", CustomerType.PREMIUM, DeliveryZone.ZONE_A), 100_000);
        assertEquals(0.25, quote.rate(), 0.0001);
        assertTrue(quote.notes().stream().anyMatch(note -> note.contains("capped at 25%")));
    }

    @Test
    void thresholdShippingComposesZoneAndFreeImplementations() {
        ShippingCalculator calculator = new ThresholdShippingCalculator(
                new ZoneShippingCalculator(), new FreeShippingCalculator());
        assertEquals(2_500.0, calculator.calculate(
                order(1_000, "", CustomerType.STANDARD, DeliveryZone.ZONE_B), 1_000).fee(), 0.001);
        ShippingQuote threshold = calculator.calculate(
                order(1_000, "", CustomerType.STANDARD, DeliveryZone.ZONE_B), 150_000);
        assertEquals(0.0, threshold.fee(), 0.001);
        assertTrue(threshold.waivedByThreshold());
        assertEquals(0.0, calculator.calculate(
                order(1_000, "FREESHIP", CustomerType.STANDARD, DeliveryZone.ZONE_C), 1_000).fee(), 0.001);
    }

    @Test
    void defaultServicePreservesTheWeekOneTotal() {
        PricingOrder pricingOrder = order(32_000, "WELCOME10", CustomerType.PREMIUM, DeliveryZone.ZONE_B);
        assertEquals(31_740.0, new OrderPricingService().calculate(pricingOrder).total(), 0.001);
    }
}
