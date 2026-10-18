package store.model;

import store.validation.OrderItemValidator;

/** One line on the order: an item, its unit price and its quantity. */
public final class PricingLineItem {

    public static final int MAX_QUANTITY = 100;

    private final String name;
    private final double unitPrice;
    private final int quantity;

    public PricingLineItem(String name, double unitPrice, int quantity) {
        OrderItemValidator.validatePricingLine(name, unitPrice, quantity, MAX_QUANTITY);
        this.name = name.trim();
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String name() {
        return name;
    }

    public double unitPrice() {
        return unitPrice;
    }

    public int quantity() {
        return quantity;
    }

    public double lineTotal() {
        return unitPrice * quantity;
    }
}
