package store.domain;

import org.junit.jupiter.api.Test;

import store.CustomerType;
import store.DeliveryZone;
import store.OrderCalculator;
import store.PriceBreakdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the important Week-2 domain rules. */
class DomainModelTest {

    private static final double D = 0.001;

    private static Address validAddress() {
        return new Address("1 Test Way", "Ikeja", "Lagos", DeliveryZone.ZONE_A);
    }

    private static Customer validCustomer() {
        return new Customer("Ada Obi", "ada@example.com", "08031234567",
                validAddress(), CustomerType.STANDARD);
    }

    private static Product stockedProduct() {
        return new Product("SKU-1", "Rice 5kg", 12000, 10);
    }

    // ---------------- records & validation ----------------

    @Test
    void addressRecordRejectsBlankCity() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Address("1 Test Way", "  ", "Lagos", DeliveryZone.ZONE_A));
        assertTrue(e.getMessage().contains("city"));
    }

    @Test
    void customerRejectsBadEmailAndPhone() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(
                "Ada", "not-an-email", "08031234567", validAddress(), CustomerType.STANDARD));
        assertThrows(IllegalArgumentException.class, () -> new Customer(
                "Ada", "ada@example.com", "abc", validAddress(), CustomerType.STANDARD));
    }

    @Test
    void loyaltyPointsRejectNegative() {
        Customer ada = validCustomer();
        assertThrows(IllegalArgumentException.class, () -> ada.earnLoyaltyPoints(-1));
    }

    @Test
    void productRejectsOversell() {
        Product rice = stockedProduct();
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> rice.decreaseStock(11));
        assertTrue(e.getMessage().contains("Not enough stock"));
    }

    // ---------------- order state machine ----------------

    @Test
    void addItemReservesStockAndCancelReturnsIt() {
        Product rice = stockedProduct();
        Order order = new Order("ORD-1", validCustomer());
        order.addItem(new OrderItem(rice, 4));
        assertEquals(6, rice.stock());
        order.cancel();
        assertEquals(10, rice.stock());
    }

    @Test
    void confirmedOrderRejectsNewItems() {
        Order order = new Order("ORD-1", validCustomer());
        order.addItem(new OrderItem(stockedProduct(), 1));
        order.confirm();
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> order.addItem(new OrderItem(stockedProduct(), 1)));
        assertTrue(e.getMessage().contains("needs PENDING"));
    }

    @Test
    void confirmRequiresAtLeastOneItem() {
        Order order = new Order("ORD-1", validCustomer());
        assertThrows(IllegalStateException.class, order::confirm);
    }

    @Test
    void shipRequiresPaidPaymentAndDelivery() {
        Product rice = stockedProduct();
        Order order = new Order("ORD-1", validCustomer());
        order.addItem(new OrderItem(rice, 1));
        order.attachPayment(new Payment("CARD", 12000));
        order.attachDelivery(new Delivery(validAddress()));
        order.confirm();
        // payment still PENDING -> ship must be refused
        assertTrue(assertThrows(IllegalStateException.class, order::ship)
                .getMessage().contains("PAID"));
        order.payment().markPaid();
        order.ship();
        assertEquals(OrderStatus.SHIPPED, order.status());
        assertEquals(DeliveryStatus.IN_TRANSIT, order.delivery().status());
    }

    @Test
    void fullLifecycleReachesDeliveredAndEarnsLoyalty() {
        Customer ada = validCustomer();
        Order order = new Order("ORD-1", ada);
        order.addItem(new OrderItem(stockedProduct(), 2));
        order.attachPayment(new Payment("TRANSFER", 24000));
        order.attachDelivery(new Delivery(validAddress()));
        order.confirm();
        order.payment().markPaid();
        order.ship();
        order.deliver();
        assertEquals(OrderStatus.DELIVERED, order.status());
        assertEquals(DeliveryStatus.DELIVERED, order.delivery().status());
        assertEquals(10, ada.loyaltyPoints());
        assertTrue(order.status().isTerminal());
    }

    @Test
    void deliveredOrderCannotBeCancelled() {
        Order order = new Order("ORD-1", validCustomer());
        order.addItem(new OrderItem(stockedProduct(), 1));
        order.attachPayment(new Payment("CARD", 12000));
        order.attachDelivery(new Delivery(validAddress()));
        order.confirm();
        order.payment().markPaid();
        order.ship();
        order.deliver();
        assertThrows(IllegalStateException.class, order::cancel);
    }

    // ---------------- payment & delivery state machines ----------------

    @Test
    void refundOnlyPossibleWhenPaid() {
        Payment payment = new Payment("CARD", 5000);
        assertThrows(IllegalStateException.class, payment::refund);
        payment.markPaid();
        payment.refund();
        assertEquals(PaymentStatus.REFUNDED, payment.status());
        assertThrows(IllegalStateException.class, payment::refund);
    }

    @Test
    void failedPaymentCanRetryThenPay() {
        Payment payment = new Payment("CARD", 5000);
        payment.markFailed();
        assertEquals(PaymentStatus.FAILED, payment.status());
        payment.markPaid();
        assertTrue(payment.isPaid());
    }

    @Test
    void deliveryCannotBeDeliveredBeforeTransit() {
        Delivery delivery = new Delivery(validAddress());
        assertThrows(IllegalStateException.class, delivery::markDelivered);
        delivery.startTransit();
        delivery.markDelivered();
        assertEquals(DeliveryStatus.DELIVERED, delivery.status());
        assertThrows(IllegalStateException.class, delivery::startTransit);
    }

    // ---------------- relationship with the Week-1 engine ----------------

    @Test
    void priceBreakdownReusesWeek1Rules() {
        Customer premium = new Customer("Ada Obi", "ada@example.com", "08031234567",
                validAddress(), CustomerType.PREMIUM);
        Order order = new Order("ORD-1", premium);
        order.addItem(new OrderItem(
                new Product("SKU-1", "Rice 5kg", 12000, 10), 2));   // 24,000
        order.addItem(new OrderItem(
                new Product("SKU-2", "Oil 1L", 4500, 8), 1));        //  4,500
        PriceBreakdown bd = order.priceBreakdown(new OrderCalculator(), "WELCOME10");
        // subtotal 28,500; 10% promo + 5% premium = 15% -> 4,275
        assertEquals(28500.0, bd.subtotal(), D);
        assertEquals(4275.0, bd.discountAmount(), D);
        // zone A fee 1,500; VAT 7.5% of 24,225 = 1,816.88
        assertEquals(1500.0, bd.deliveryFee(), D);
        assertEquals(28500 - 4275 + 1500 + 1816.88, bd.total(), D);
    }
}
