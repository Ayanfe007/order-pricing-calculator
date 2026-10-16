package store.service;

import org.junit.jupiter.api.Test;

import store.domain.Payment;
import store.domain.PaymentStatus;
import store.exception.UnsupportedPaymentMethodException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentProcessingServiceTest {
    private final PaymentProcessingService service = new PaymentProcessingService();

    @Test
    void cardProcessorHandlesCardPayment() {
        Payment payment = new Payment("CARD", 12_000);
        service.process(payment);
        assertEquals(PaymentStatus.PAID, payment.status());
    }

    @Test
    void transferProcessorHandlesBankTransfer() {
        Payment payment = new Payment("TRANSFER", 12_000);
        service.process(payment);
        assertEquals(PaymentStatus.PAID, payment.status());
    }

    @Test
    void unknownMethodIsRejectedWithoutChangingPaymentState() {
        Payment payment = new Payment("CRYPTO", 12_000);
        assertThrows(UnsupportedPaymentMethodException.class, () -> service.process(payment));
        assertEquals(PaymentStatus.PENDING, payment.status());
    }
}
