package store.service;

import java.util.Objects;

import store.model.PricingOrder;
import store.util.PricingConfig;

/**
 * Decorates zone pricing with the existing free-shipping promo and subtotal
 * threshold. The zone and free calculators remain independently replaceable.
 */
public final class ThresholdShippingCalculator implements ShippingCalculator {
    private final ShippingCalculator zoneCalculator;
    private final ShippingCalculator freeCalculator;

    public ThresholdShippingCalculator(ShippingCalculator zoneCalculator,
                                       ShippingCalculator freeCalculator) {
        this.zoneCalculator = Objects.requireNonNull(zoneCalculator, "zoneCalculator");
        this.freeCalculator = Objects.requireNonNull(freeCalculator, "freeCalculator");
    }

    @Override
    public ShippingQuote calculate(PricingOrder order, double discountedSubtotal) {
        if (PricingConfig.PROMO_FREE_DELIVERY.equals(order.promoCode())) {
            return freeCalculator.calculate(order, discountedSubtotal);
        }
        if (discountedSubtotal >= PricingConfig.FREE_DELIVERY_THRESHOLD) {
            return new ShippingQuote(
                    freeCalculator.calculate(order, discountedSubtotal).fee(), true);
        }
        return zoneCalculator.calculate(order, discountedSubtotal);
    }
}
