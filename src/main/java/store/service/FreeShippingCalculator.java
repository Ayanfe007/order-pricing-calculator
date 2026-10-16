package store.service;

/** A composable strategy that waives the delivery fee. */
public final class FreeShippingCalculator implements ShippingCalculator {
    @Override
    public ShippingQuote calculate(store.Order order, double discountedSubtotal) {
        return new ShippingQuote(0.0, false);
    }
}
