package store.validation;

import java.util.List;

import store.exception.ValidationException;

/** Validation shared by the aggregate order and the pricing request. */
public final class OrderValidator {
    private OrderValidator() { }

    public static void validateDomainOrder(String orderId, boolean hasCustomer) {
        if (orderId == null || orderId.isBlank()) {
            throw new ValidationException("Order id must not be blank.");
        }
        if (!hasCustomer) {
            throw new ValidationException("Order needs a customer.");
        }
    }

    public static void validatePricingOrder(List<?> items, String zone) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("An order must contain at least one item.");
        }
        if (zone == null) {
            throw new ValidationException("A delivery zone is required (A, B or C).");
        }
    }
}
