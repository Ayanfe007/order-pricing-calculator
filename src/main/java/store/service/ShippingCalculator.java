package store.service;

import store.Order;

/** Strategy interface for calculating an order's delivery fee. */
public interface ShippingCalculator {
    ShippingQuote calculate(Order order, double discountedSubtotal);
}
