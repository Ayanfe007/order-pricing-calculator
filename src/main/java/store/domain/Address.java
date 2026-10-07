package store.domain;

import store.DeliveryZone;

/**
 * Immutable address - a record, because an address snapshot on an order
 * should never silently change. Validation lives in the compact constructor.
 */
public record Address(String street, String city, String state, DeliveryZone zone) {

    public Address {
        requireText(street, "street");
        requireText(city, "city");
        requireText(state, "state");
        if (zone == null) {
            throw new IllegalArgumentException("Address zone must not be null.");
        }
    }

    private static void requireText(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Address " + what + " must not be blank.");
        }
    }
}
