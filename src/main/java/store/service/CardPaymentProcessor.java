package store.service;

import java.util.Locale;

import store.domain.Payment;
import store.exception.UnsupportedPaymentMethodException;
import store.exception.ValidationException;

/** Local card-payment adapter; marks the payment paid without calling a gateway. */
public final class CardPaymentProcessor implements PaymentProcessor {
    @Override
    public boolean supports(String method) {
        return method != null && "CARD".equals(method.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public void process(Payment payment) {
        if (payment == null) {
            throw new ValidationException("Payment is required.");
        }
        if (!supports(payment.method())) {
            throw new UnsupportedPaymentMethodException(
                    "Card processor cannot process method '" + payment.method() + "'.");
        }
        payment.markPaid();
    }
}
