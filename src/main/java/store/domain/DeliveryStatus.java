package store.domain;

/** Fixed vocabulary of delivery states; guarded inside {@link Delivery}. */
public enum DeliveryStatus {
    PREPARING,
    IN_TRANSIT,
    DELIVERED,
    FAILED
}
