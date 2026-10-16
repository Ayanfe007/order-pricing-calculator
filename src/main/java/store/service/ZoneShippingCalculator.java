package store.service;

import java.util.List;

import store.Order;

/** Charges the base fee for the order's selected delivery zone. */
public final class ZoneShippingCalculator implements ShippingCalculator {
    @Override
    public ShippingQuote calculate(Order order, double discountedSubtotal) {
        return new ShippingQuote(order.zone().baseFee(), false);
    }
}
