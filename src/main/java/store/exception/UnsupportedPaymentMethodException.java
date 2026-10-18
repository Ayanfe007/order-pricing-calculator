package store.exception;

/** No configured payment processor can handle the requested payment method. */
public final class UnsupportedPaymentMethodException extends ValidationException {
    private static final long serialVersionUID = 1L;

    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }
}
