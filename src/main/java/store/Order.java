package store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import store.validation.OrderValidator;

/** A validated order: at least one item, plus promo code, customer type and zone. */
public final class Order {

    private final List<OrderItem> items;
    private final String promoCode;
    private final CustomerType customerType;
    private final DeliveryZone zone;

    public Order(List<OrderItem> items, String promoCode,
                 CustomerType customerType, DeliveryZone zone) {
        OrderValidator.validatePricingOrder(items, zone == null ? null : zone.name());
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.promoCode = promoCode == null ? "" : promoCode.trim().toUpperCase();
        this.customerType = customerType == null ? CustomerType.STANDARD : customerType;
        this.zone = zone;
    }

    public List<OrderItem> items() {
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
