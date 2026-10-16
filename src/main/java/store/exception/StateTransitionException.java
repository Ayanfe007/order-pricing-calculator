package store.exception;

/** A requested lifecycle operation is not valid for the object's current state. */
public class StateTransitionException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public StateTransitionException(String message) {
        super(message);
    }
}
