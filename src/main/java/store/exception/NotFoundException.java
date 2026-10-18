package store.exception;

/** A requested entity does not exist in its repository. */
public final class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
