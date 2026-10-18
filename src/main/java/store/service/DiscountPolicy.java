package store.service;

import store.model.PricingOrder;

/** Strategy interface for one source of an order discount. */
public interface DiscountPolicy {
    DiscountQuote evaluate(PricingOrder order, double subtotal);
}
