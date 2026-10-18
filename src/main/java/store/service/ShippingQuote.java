package store.service;

/** Delivery fee and whether the free-delivery threshold caused the waiver. */
public record ShippingQuote(double fee, boolean waivedByThreshold) {
    public ShippingQuote {
        if (Double.isNaN(fee) || fee < 0.0) {
            throw new IllegalArgumentException("Shipping fee cannot be negative or NaN.");
        }
    }
}
