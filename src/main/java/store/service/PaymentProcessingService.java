package store.service;

import java.util.List;
import java.util.Objects;

import store.model.Payment;
import store.exception.UnsupportedPaymentMethodException;
import store.exception.ValidationException;

/** Routes a payment to the first registered processor that supports its method. */
public final class PaymentProcessingService {
    private final List<PaymentProcessor> processors;

    public PaymentProcessingService() {
        this(List.of(new CardPaymentProcessor(), new BankTransferPaymentProcessor()));
    }

    public PaymentProcessingService(List<PaymentProcessor> processors) {
        if (processors == null || processors.isEmpty()) {
            throw new IllegalArgumentException("At least one payment processor is required.");
        }
        this.processors = List.copyOf(processors);
        this.processors.forEach(Objects::requireNonNull);
    }

    public void process(Payment payment) {
        if (payment == null) {
            throw new ValidationException("Payment is required.");
        }
        PaymentProcessor processor = processors.stream()
                .filter(candidate -> candidate.supports(payment.method()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedPaymentMethodException(
                        "No payment processor is registered for method '" + payment.method() + "'."));
        processor.process(payment);
    }
}
