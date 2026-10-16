package store.model;

import store.validation.AddressValidator;

/**
 * Immutable address - a record, because an address snapshot on an order
 * should never silently change. Validation lives in the compact constructor.
 */
public record Address(String street, String city, String state, DeliveryZone zone) {

    public Address {
        AddressValidator.validate(street, city, state, zone != null);
    }

}
