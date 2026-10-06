package store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of pricing one order. Pure data - all printing happens in the
 * console layer, which is what keeps the business logic testable.
 */
public final class PriceBreakdown {

    private final double subtotal;
    private final double discountRate;
    private final double discountAmount;
    private final double discountedSubtotal;
    private final double deliveryFee;
    private final double taxAmount;
    private final double total;
    private final List<String> notes;

    PriceBreakdown(double subtotal, double discountRate, double discountAmount,
                   double discountedSubtotal, double deliveryFee, double taxAmount,
                   double total, List<String> notes) {
        this.subtotal = subtotal;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedSubtotal = discountedSubtotal;
        this.deliveryFee = deliveryFee;
        this.taxAmount = taxAmount;
        this.total = total;
        this.notes = Collections.unmodifiableList(new ArrayList<>(notes));
    }

    public double subtotal() {
        return subtotal;
    }

    /** Combined discount rate actually applied (after the 25% cap). */
    public double discountRate() {
        return discountRate;
    }

    public double discountAmount() {
        return discountAmount;
    }

    public double discountedSubtotal() {
        return discountedSubtotal;
    }

    public double deliveryFee() {
        return deliveryFee;
    }

    public double taxAmount() {
        return taxAmount;
    }

    public double total() {
        return total;
    }

    /** Human-readable messages describing which rules fired (or were ignored). */
    public List<String> notes() {
        return notes;
    }
}
