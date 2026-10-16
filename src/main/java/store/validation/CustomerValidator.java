package store.validation;

import java.util.regex.Pattern;

import store.exception.ValidationException;

/** Central customer contact and identity rules. */
public final class CustomerValidator {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");

    private CustomerValidator() { }

    public static void validate(String name, String email, String phone, boolean hasAddress) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Customer name must not be blank.");
        }
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new ValidationException(
                    "Customer email '" + email + "' is not a valid email address.");
        }
        if (phone == null || !PHONE.matcher(phone.trim()).matches()) {
            throw new ValidationException(
                    "Customer phone '" + phone + "' must be 7-15 digits (optional +).");
        }
        if (!hasAddress) {
            throw new ValidationException("Customer address is required.");
        }
    }
}
