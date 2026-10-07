package store.domain;

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
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("Payment method must not be blank.");
        }
        if (Double.isNaN(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be a positive amount, got " + amount + ".");
        }
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
            throw new IllegalStateException(
                    "Cannot pay a payment that is " + status + ".");
        }
        status = PaymentStatus.PAID;
    }

    public void markFailed() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a PENDING payment can fail; this one is " + status + ".");
        }
        status = PaymentStatus.FAILED;
    }

    public void refund() {
        if (status != PaymentStatus.PAID) {
            throw new IllegalStateException(
                    "Only a PAID payment can be refunded; this one is " + status + ".");
        }
        status = PaymentStatus.REFUNDED;
    }
}
