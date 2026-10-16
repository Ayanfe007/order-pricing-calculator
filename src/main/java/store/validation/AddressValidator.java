package store.validation;

import store.exception.ValidationException;

/** Validation rules for the immutable address value object. */
public final class AddressValidator {
    private AddressValidator() { }

    public static void validate(String street, String city, String state, String zone) {
        requireText(street, "street");
        requireText(city, "city");
        requireText(state, "state");
        if (zone == null) {
            throw new ValidationException("Address zone must not be null.");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Address " + field + " must not be blank.");
        }
    }
}
