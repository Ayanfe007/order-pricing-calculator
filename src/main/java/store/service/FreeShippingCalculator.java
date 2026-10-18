package store.service;

import store.model.PricingOrder;

/** A composable strategy that waives the delivery fee. */
public final class FreeShippingCalculator implements ShippingCalculator {
    @Override
    public ShippingQuote calculate(PricingOrder order, double discountedSubtotal) {
        return new ShippingQuote(0.0, false);
    }
}
