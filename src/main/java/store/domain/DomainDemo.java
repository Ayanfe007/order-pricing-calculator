package store.domain;

import store.DeliveryZone;
import store.CustomerType;
import store.OrderCalculator;
import store.PriceBreakdown;

import java.util.Locale;

/**
 * Small console demo of the Week-2 domain model: builds a customer, products,
 * an order, a payment and a delivery, shows a guarded refusal, then walks the
 * happy-path lifecycle and prices the order with the Week-1 engine.
 */
public final class DomainDemo {

    public static void main(String[] args) {
        OrderCalculator calculator = new OrderCalculator();

        Address home = new Address("12 Bode Thomas St", "Surulere", "Lagos", DeliveryZone.ZONE_B);
        Customer ada = new Customer("Ada Obi", "ada@example.com", "08031234567",
                home, CustomerType.PREMIUM);
        Product rice = new Product("GC-001", "Golden Rice 5kg", 12000, 10);
        Product oil = new Product("GC-002", "Groundnut Oil 1L", 4500, 8);

        Order order = new Order("ORD-2026-0001", ada);
        order.addItem(new OrderItem(rice, 2));
        order.addItem(new OrderItem(oil, 1));
        order.attachPayment(new Payment("CARD", 40000));
        order.attachDelivery(new Delivery(home));

        System.out.println("Stock after reserving items: rice=" + rice.stock()
                + ", oil=" + oil.stock());

        PriceBreakdown bd = order.priceBreakdown(calculator, "WELCOME10");
        System.out.println("Week-1 engine prices it: subtotal=" + money(bd.subtotal())
                + ", discount=" + money(bd.discountAmount())
                + ", delivery=" + money(bd.deliveryFee())
                + ", VAT=" + money(bd.taxAmount())
                + ", TOTAL=" + money(bd.total()));

        try {
            order.ship(); // must be refused: not confirmed, payment not paid
        } catch (IllegalStateException e) {
            System.out.println("Guarded refusal: " + e.getMessage());
        }

        order.confirm();
        order.payment().markPaid();
        order.ship();
        order.deliver();

        System.out.println("Final order status:    " + order.status());
        System.out.println("Final delivery status: " + order.delivery().status());
        System.out.println("Final payment status:  " + order.payment().status());
        System.out.println("Ada's loyalty points:  " + ada.loyaltyPoints());
    }

    private static String money(double amount) {
        return String.format(Locale.US, "NGN %,.2f", amount);
    }
}
