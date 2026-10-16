package store.service;

import java.util.List;

import store.model.PricingOrder;
import store.util.PricingConfig;
import store.util.PricingFormat;

/** Applies the two inclusive subtotal tiers from the pricing rules. */
public final class TieredDiscountPolicy implements DiscountPolicy {
    public double rateFor(double subtotal) {
        if (subtotal >= PricingConfig.TIER2_THRESHOLD) {
            return PricingConfig.TIER2_RATE;
        }
        if (subtotal >= PricingConfig.TIER1_THRESHOLD) {
            return PricingConfig.TIER1_RATE;
        }
        return 0.0;
    }

    @Override
    public DiscountQuote evaluate(PricingOrder order, double subtotal) {
        double rate = rateFor(subtotal);
        if (rate == 0.0) {
            return new DiscountQuote(0.0, List.of());
        }
        double threshold = rate == PricingConfig.TIER2_RATE
                ? PricingConfig.TIER2_THRESHOLD : PricingConfig.TIER1_THRESHOLD;
        String note = "Order-value discount: " + PricingFormat.percent(rate) + " (subtotal of "
                + PricingFormat.money(subtotal) + " is above " + PricingFormat.money(threshold) + ").";
        return new DiscountQuote(rate, List.of(note));
    }
}
