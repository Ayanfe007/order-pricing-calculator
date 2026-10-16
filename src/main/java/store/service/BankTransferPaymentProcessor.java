package store.service;

import java.util.Locale;

import store.domain.Payment;
import store.exception.UnsupportedPaymentMethodException;
import store.exception.ValidationException;

/** Local bank-transfer adapter; marks the payment paid without calling a bank. */
public final class BankTransferPaymentProcessor implements PaymentProcessor {
    @Override
    public boolean supports(String method) {
        if (method == null) {
            return false;
        }
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        return "TRANSFER".equals(normalized) || "BANK_TRANSFER".equals(normalized);
    }

    @Override
    public void process(Payment payment) {
        if (payment == null) {
            throw new ValidationException("Payment is required.");
        }
        if (!supports(payment.method())) {
            throw new UnsupportedPaymentMethodException(
                    "Bank-transfer processor cannot process method '" + payment.method() + "'.");
        }
        payment.markPaid();
    }
}
