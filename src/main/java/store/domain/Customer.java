package store.domain;

import store.CustomerType;
import store.validation.CustomerValidator;
import store.exception.ValidationException;

/**
 * A class (not a record) because a customer has changing state:
 * loyalty points grow as orders are delivered.
 */
public final class Customer {

    private final String name;
    private final String email;
    private final String phone;
    private final Address address;
    private final CustomerType type;
    private int loyaltyPoints;

    public Customer(String name, String email, String phone,
                    Address address, CustomerType type) {
        CustomerValidator.validate(name, email, phone, address != null);
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
            throw new ValidationException("Loyalty points cannot be negative.");
        }
        loyaltyPoints += points;
    }
}
