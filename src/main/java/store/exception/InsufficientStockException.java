package store.exception;

/** A stock reservation would make a product's available quantity negative. */
public final class InsufficientStockException extends StateTransitionException {
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(message);
    }
}
