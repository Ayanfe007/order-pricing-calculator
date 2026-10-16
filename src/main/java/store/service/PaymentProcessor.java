package store.service;

import store.model.Payment;

/** Strategy for settling one supported payment method. */
public interface PaymentProcessor {
    boolean supports(String method);

    /** Processes a payment and updates its domain state on success. */
    void process(Payment payment);
}
