package store.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import store.validation.OrderValidator;

/** A validated order: at least one item, plus promo code, customer type and zone. */
public final class PricingOrder {

    private final List<PricingLineItem> items;
    private final String promoCode;
    private final CustomerType customerType;
    private final DeliveryZone zone;

    public PricingOrder(List<PricingLineItem> items, String promoCode,
                 CustomerType customerType, DeliveryZone zone) {
        OrderValidator.validatePricingOrder(items, zone != null);
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.promoCode = promoCode == null ? "" : promoCode.trim().toUpperCase(Locale.ROOT);
        this.customerType = customerType == null ? CustomerType.STANDARD : customerType;
        this.zone = zone;
    }

    public List<PricingLineItem> items() {
        return items;
    }

    /** Normalised promo code; empty string when the customer has none. */
    public String promoCode() {
        return promoCode;
    }

    public CustomerType customerType() {
        return customerType;
    }

    public DeliveryZone zone() {
        return zone;
    }
}
