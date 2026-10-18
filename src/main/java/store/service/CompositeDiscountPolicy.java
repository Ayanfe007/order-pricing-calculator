package store.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import store.model.PricingOrder;
import store.util.PricingFormat;

/** Composes independent discount rules and enforces the common maximum once. */
public final class CompositeDiscountPolicy implements DiscountPolicy {
    private final List<DiscountPolicy> policies;
    private final double maximumRate;

    public CompositeDiscountPolicy(List<DiscountPolicy> policies, double maximumRate) {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("At least one discount policy is required.");
        }
        if (Double.isNaN(maximumRate) || maximumRate < 0.0 || maximumRate > 1.0) {
            throw new IllegalArgumentException("Maximum discount rate must be between 0 and 1.");
        }
        this.policies = List.copyOf(policies);
        this.policies.forEach(Objects::requireNonNull);
        this.maximumRate = maximumRate;
    }

    @Override
    public DiscountQuote evaluate(PricingOrder order, double subtotal) {
        double combinedRate = 0.0;
        List<String> notes = new ArrayList<>();
        for (DiscountPolicy policy : policies) {
            DiscountQuote quote = policy.evaluate(order, subtotal);
            combinedRate += quote.rate();
            notes.addAll(quote.notes());
        }
        double appliedRate = Math.min(combinedRate, maximumRate);
        if (combinedRate > maximumRate) {
            notes.add("Combined discounts capped at " + PricingFormat.percent(maximumRate) + ".");
        }
        return new DiscountQuote(appliedRate, notes);
    }
}
