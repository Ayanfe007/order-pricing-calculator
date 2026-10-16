package store.service;

import java.util.List;

import store.model.PricingOrder;

/** Charges the base fee for the order's selected delivery zone. */
public final class ZoneShippingCalculator implements ShippingCalculator {
    @Override
    public ShippingQuote calculate(PricingOrder order, double discountedSubtotal) {
        return new ShippingQuote(order.zone().baseFee(), false);
    }
}
