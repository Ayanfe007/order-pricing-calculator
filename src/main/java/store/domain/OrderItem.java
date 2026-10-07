package store.domain;

/**
 * Immutable order line - a record pairing a product with a quantity.
 * The quantity is validated here; stock reservation happens in {@link Order}.
 */
public record OrderItem(Product product, int quantity) {

    public static final int MAX_QUANTITY = 100;

    public OrderItem {
        if (product == null) {
            throw new IllegalArgumentException("Order item needs a product.");
        }
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "Order item quantity must be between 1 and " + MAX_QUANTITY
                            + ", got " + quantity + ".");
        }
    }

    public double lineTotal() {
        return product.unitPrice() * quantity;
    }
}
