package store.domain;

/**
 * Fixed vocabulary of order states. Transitions between these states are
 * guarded inside {@link Order}, not here - the enum only names the states.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /** True once no further transition is allowed. */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
}
