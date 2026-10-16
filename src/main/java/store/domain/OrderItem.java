package store.domain;

import store.validation.OrderItemValidator;

/**
 * Immutable order line - a record pairing a product with a quantity.
 * The quantity is validated here; stock reservation happens in {@link Order}.
 */
public record OrderItem(Product product, int quantity) {

    public static final int MAX_QUANTITY = 100;

    public OrderItem {
        OrderItemValidator.validateProductLine(product != null, quantity, MAX_QUANTITY);
    }

    public double lineTotal() {
        return product.unitPrice() * quantity;
    }
}
