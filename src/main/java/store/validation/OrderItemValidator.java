package store.validation;

import store.exception.ValidationException;

/** Validation for both catalog-priced lines and product-backed order lines. */
public final class OrderItemValidator {
    private OrderItemValidator() { }

    public static void validatePricingLine(String name, double unitPrice, int quantity, int maxQuantity) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Item name must not be empty.");
        }
        if (Double.isNaN(unitPrice) || unitPrice <= 0) {
            throw new ValidationException(
                    "Price for '" + name.trim() + "' must be a positive amount, got "
                            + unitPrice + ".");
        }
        if (quantity < 1) {
            throw new ValidationException(
                    "Quantity for '" + name.trim() + "' must be at least 1, got "
                            + quantity + ".");
        }
        if (quantity > maxQuantity) {
            throw new ValidationException("Quantity for '" + name.trim() + "' must not exceed "
                    + maxQuantity + ", got " + quantity + ".");
        }
    }

    public static void validateProductLine(boolean hasProduct, int quantity, int maxQuantity) {
        if (!hasProduct) {
            throw new ValidationException("Order item needs a product.");
        }
        if (quantity < 1 || quantity > maxQuantity) {
            throw new ValidationException("Order item quantity must be between 1 and " + maxQuantity
                    + ", got " + quantity + ".");
        }
    }
}
