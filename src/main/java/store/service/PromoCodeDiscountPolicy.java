package store.service;

import java.util.List;

import store.model.PricingOrder;
import store.util.PricingConfig;
import store.util.PricingFormat;

/** Applies percentage promo codes; FREESHIP is handled by the shipping strategy. */
public final class PromoCodeDiscountPolicy implements DiscountPolicy {
    public double rateFor(String promoCode) {
        Double rate = PricingConfig.PROMO_CODES.get(promoCode);
        return rate == null ? 0.0 : rate;
    }

    @Override
    public DiscountQuote evaluate(PricingOrder order, double subtotal) {
        String code = order.promoCode();
        double rate = rateFor(code);
        if (code.isEmpty()) {
            return new DiscountQuote(0.0, List.of());
        }
        if (rate > 0.0) {
            return new DiscountQuote(rate,
                    List.of("Promo code " + code + ": " + PricingFormat.percent(rate) + " discount."));
        }
        if (PricingConfig.PROMO_FREE_DELIVERY.equals(code)) {
            return new DiscountQuote(0.0, List.of("Promo code FREESHIP: delivery fee waived."));
        }
        return new DiscountQuote(0.0,
                List.of("Promo code '" + code + "' not recognised - ignored."));
    }
}
