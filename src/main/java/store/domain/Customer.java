package store.domain;

import java.util.regex.Pattern;

import store.CustomerType;

/**
 * A class (not a record) because a customer has changing state:
 * loyalty points grow as orders are delivered.
 */
public final class Customer {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");

    private final String name;
    private final String email;
    private final String phone;
    private final Address address;
    private final CustomerType type;
    private int loyaltyPoints;

    public Customer(String name, String email, String phone,
                    Address address, CustomerType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank.");
        }
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Customer email '" + email + "' is not a valid email address.");
        }
        if (phone == null || !PHONE.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Customer phone '" + phone + "' must be 7-15 digits (optional +).");
        }
        if (address == null) {
            throw new IllegalArgumentException("Customer address is required.");
        }
        this.name = name.trim();
        this.email = email.trim();
        this.phone = phone.trim();
        this.address = address;
        this.type = type == null ? CustomerType.STANDARD : type;
        this.loyaltyPoints = 0;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public Address address() {
        return address;
    }

    /** Re-uses the Week-1 customer-type enum (standard / premium / student). */
    public CustomerType type() {
        return type;
    }

    public int loyaltyPoints() {
        return loyaltyPoints;
    }

    public void earnLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Loyalty points cannot be negative.");
        }
        loyaltyPoints += points;
    }
}
