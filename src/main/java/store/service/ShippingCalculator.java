package store.service;

import store.model.PricingOrder;

/** Strategy interface for calculating an order's delivery fee. */
public interface ShippingCalculator {
    ShippingQuote calculate(PricingOrder order, double discountedSubtotal);
}
