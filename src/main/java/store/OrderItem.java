package store;

/** One line on the order: an item, its unit price and its quantity. */
public final class OrderItem {

    public static final int MAX_QUANTITY = 100;

    private final String name;
    private final double unitPrice;
    private final int quantity;

    public OrderItem(String name, double unitPrice, int quantity) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name must not be empty.");
        }
        if (Double.isNaN(unitPrice) || unitPrice <= 0) {
            throw new IllegalArgumentException(
                    "Price for '" + name.trim() + "' must be a positive amount, got "
                            + unitPrice + ".");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity for '" + name.trim() + "' must be at least 1, got "
                            + quantity + ".");
        }
        if (quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "Quantity for '" + name.trim() + "' must not exceed " + MAX_QUANTITY
                            + ", got " + quantity + ".");
        }
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
