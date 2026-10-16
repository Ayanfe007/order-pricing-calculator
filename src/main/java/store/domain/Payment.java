package store.domain;

import store.exception.StateTransitionException;
import store.validation.PaymentValidator;

/**
 * A class with a guarded state machine:
 * PENDING -> PAID (or FAILED, with retry to PAID), PAID -> REFUNDED.
 * Every illegal transition is refused with a clear message.
 */
public final class Payment {

    private final String method;
    private final double amount;
    private PaymentStatus status = PaymentStatus.PENDING;

    public Payment(String method, double amount) {
        PaymentValidator.validate(method, amount);
        this.method = method.trim();
        this.amount = amount;
    }

    public String method() {
        return method;
    }

    public double amount() {
        return amount;
    }

    public PaymentStatus status() {
        return status;
    }

    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }

    public void markPaid() {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.FAILED) {
            throw new StateTransitionException(
                    "Cannot pay a payment that is " + status + ".");
        }
        status = PaymentStatus.PAID;
    }

    public void markFailed() {
        if (status != PaymentStatus.PENDING) {
            throw new StateTransitionException(
                    "Only a PENDING payment can fail; this one is " + status + ".");
        }
        status = PaymentStatus.FAILED;
    }

    public void refund() {
        if (status != PaymentStatus.PAID) {
            throw new StateTransitionException(
                    "Only a PAID payment can be refunded; this one is " + status + ".");
        }
        status = PaymentStatus.REFUNDED;
    }
}
