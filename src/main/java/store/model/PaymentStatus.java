package store.model;

/** Fixed vocabulary of payment states; guarded inside {@link Payment}. */
public enum PaymentStatus {
    PENDING,
    PAID,
    REFUNDED,
    FAILED
}
