package store.exception;

/** An invalid value supplied at an application boundary or object construction. */
public class ValidationException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
