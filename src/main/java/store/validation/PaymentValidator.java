package store.validation;

import store.exception.ValidationException;

/** Input rules for creating a payment. */
public final class PaymentValidator {
    private PaymentValidator() { }

    public static void validate(String method, double amount) {
        if (method == null || method.isBlank()) {
            throw new ValidationException("Payment method must not be blank.");
        }
        if (Double.isNaN(amount) || amount <= 0) {
            throw new ValidationException(
                    "Payment amount must be a positive amount, got " + amount + ".");
        }
    }
}
