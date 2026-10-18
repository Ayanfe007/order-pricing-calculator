package store.app;

import java.util.Locale;

import store.model.Address;
import store.model.Customer;
import store.model.CustomerType;
import store.model.Delivery;
import store.model.DeliveryZone;
import store.model.Order;
import store.model.OrderItem;
import store.model.Payment;
import store.model.PriceBreakdown;
import store.model.Product;
import store.service.OrderPricingService;
import store.service.PaymentProcessingService;

/** Runnable example of the Week-2 order lifecycle using the Week-3 services. */
public final class DomainDemo {
    private DomainDemo() { }

    public static void main(String[] args) {
        OrderPricingService pricing = new OrderPricingService();
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
        PriceBreakdown breakdown = pricing.calculate(order, "WELCOME10");
        System.out.println("Week-1 rules price it: subtotal=" + money(breakdown.subtotal())
                + ", discount=" + money(breakdown.discountAmount())
                + ", delivery=" + money(breakdown.deliveryFee())
                + ", VAT=" + money(breakdown.taxAmount())
                + ", TOTAL=" + money(breakdown.total()));

        try {
            order.ship();
        } catch (IllegalStateException e) {
            System.out.println("Guarded refusal: " + e.getMessage());
        }

        order.confirm();
        new PaymentProcessingService().process(order.payment());
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
