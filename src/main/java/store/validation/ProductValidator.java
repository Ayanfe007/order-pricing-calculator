package store.validation;

import store.exception.ValidationException;

/** SKU, product name, money, and inventory invariants. */
public final class ProductValidator {
    private ProductValidator() { }

    public static void validate(String sku, String name, double unitPrice, int stock) {
        if (sku == null || sku.isBlank()) {
            throw new ValidationException("Product SKU must not be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Product name must not be blank.");
        }
        if (Double.isNaN(unitPrice) || unitPrice <= 0) {
            throw new ValidationException(
                    "Product price must be a positive amount, got " + unitPrice + ".");
        }
        if (stock < 0) {
            throw new ValidationException("Product stock cannot be negative, got " + stock + ".");
        }
    }
}
