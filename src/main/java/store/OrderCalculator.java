package store;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure business logic: no reading from the keyboard, no printing.
 * The console front-end ({@link ConsoleApp}) only collects input and
 * displays the {@link PriceBreakdown} produced here.
 */
public final class OrderCalculator {

    /** Sum of price x quantity over all items, rounded to 2 decimals. */
    public double subtotal(Order order) {
        double sum = 0;
        for (OrderItem item : order.items()) {
            sum += item.lineTotal();
        }
        return round2(sum);
    }

    /** Rule 1: tiered order-value discount (thresholds are inclusive). */
    public double tieredRate(double subtotal) {
        if (subtotal >= PricingConfig.TIER2_THRESHOLD) {
            return PricingConfig.TIER2_RATE;
        }
        if (subtotal >= PricingConfig.TIER1_THRESHOLD) {
            return PricingConfig.TIER1_RATE;
        }
        return 0.0;
    }

    /** Rule 2: promo-code discount (0 when the code is unknown or absent). */
    public double promoRate(String promoCode) {
        Double rate = PricingConfig.PROMO_CODES.get(promoCode);
        return rate == null ? 0.0 : rate;
    }

    /** Combined percentage discount, capped at the business maximum. */
    public double discountRate(Order order, double subtotal) {
        double combined =
                tieredRate(subtotal) + promoRate(order.promoCode()) + order.customerType().rate();
        return Math.min(combined, PricingConfig.MAX_DISCOUNT_RATE);
    }

    /** True when the FREESHIP promo or the free-delivery threshold applies. */
    public boolean deliveryWaived(Order order, double discountedSubtotal) {
        return PricingConfig.PROMO_FREE_DELIVERY.equals(order.promoCode())
                || discountedSubtotal >= PricingConfig.FREE_DELIVERY_THRESHOLD;
    }

    /** Location-based base fee, waived when the order-value rule applies. */
    public double deliveryFee(Order order, double discountedSubtotal) {
        return deliveryWaived(order, discountedSubtotal) ? 0.0 : order.zone().baseFee();
    }

    /** VAT on the discounted goods amount (the delivery fee is not taxed). */
    public double tax(double discountedSubtotal) {
        return round2(discountedSubtotal * PricingConfig.TAX_RATE);
    }

    /** Prices a whole order and records which rules fired, as plain messages. */
    public PriceBreakdown calculate(Order order) {
        List<String> notes = new ArrayList<>();

        double subtotal = subtotal(order);

        double tiered = tieredRate(subtotal);
        if (tiered > 0) {
            double threshold = tiered == PricingConfig.TIER2_RATE
                    ? PricingConfig.TIER2_THRESHOLD : PricingConfig.TIER1_THRESHOLD;
            notes.add("Order-value discount: " + pct(tiered) + " (subtotal of "
                    + money(subtotal) + " is above " + money(threshold) + ").");
        }

        double promo = promoRate(order.promoCode());
        if (!order.promoCode().isEmpty()) {
            if (promo > 0) {
                notes.add("Promo code " + order.promoCode() + ": " + pct(promo) + " discount.");
            } else if (PricingConfig.PROMO_FREE_DELIVERY.equals(order.promoCode())) {
                notes.add("Promo code FREESHIP: delivery fee waived.");
            } else {
                notes.add("Promo code '" + order.promoCode() + "' not recognised - ignored.");
            }
        }

        double member = order.customerType().rate();
        if (member > 0) {
            notes.add(order.customerType().label() + " discount: " + pct(member) + ".");
        }

        double combined = tiered + promo + member;
        double rate = Math.min(combined, PricingConfig.MAX_DISCOUNT_RATE);
        if (combined > PricingConfig.MAX_DISCOUNT_RATE) {
            notes.add("Combined discounts capped at " + pct(PricingConfig.MAX_DISCOUNT_RATE) + ".");
        }

        double discountAmount = round2(subtotal * rate);
        double discounted = round2(subtotal - discountAmount);

        double fee = deliveryFee(order, discounted);
        if (fee == 0.0 && !PricingConfig.PROMO_FREE_DELIVERY.equals(order.promoCode())) {
            notes.add("Free delivery: discounted order value of " + money(discounted)
                    + " is above " + money(PricingConfig.FREE_DELIVERY_THRESHOLD) + ".");
        }

        double taxAmount = tax(discounted);
        double total = round2(discounted + fee + taxAmount);

        return new PriceBreakdown(subtotal, rate, discountAmount, discounted,
                fee, taxAmount, total, notes);
    }

    /** Rounds to two decimal places (kobo). */
    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String pct(double rate) {
        return String.format(Locale.US, "%.0f%%", rate * 100);
    }

    private static String money(double amount) {
        return String.format(Locale.US, "%s %,.2f", PricingConfig.CURRENCY, amount);
    }
}
