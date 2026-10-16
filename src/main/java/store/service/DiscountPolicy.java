package store.service;

import store.Order;

/** Strategy interface for one source of an order discount. */
public interface DiscountPolicy {
    DiscountQuote evaluate(Order order, double subtotal);
}
