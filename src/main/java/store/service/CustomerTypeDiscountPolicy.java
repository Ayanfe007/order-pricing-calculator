package store.service;

import java.util.List;

import store.model.PricingOrder;
import store.util.PricingFormat;

/** Applies a customer-category discount when the customer type has one. */
public final class CustomerTypeDiscountPolicy implements DiscountPolicy {
    @Override
    public DiscountQuote evaluate(PricingOrder order, double subtotal) {
        double rate = order.customerType().rate();
        if (rate == 0.0) {
            return new DiscountQuote(0.0, List.of());
        }
        return new DiscountQuote(rate,
                List.of(order.customerType().label() + " discount: "
                        + PricingFormat.percent(rate) + "."));
    }
}
