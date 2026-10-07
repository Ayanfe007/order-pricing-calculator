package store.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * The aggregate root of the domain model. A class because its state changes
 * through a guarded lifecycle:
 *
 *   PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
 *   PENDING/CONFIRMED/SHIPPED -> CANCELLED
 *
 * Every state change is an encapsulated validation method: illegal moves
 * throw IllegalStateException with a message that explains the rule.
 */
public final class Order {

    private final String orderId;
    private final Customer customer;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.PENDING;
    private Payment payment;
    private Delivery delivery;

    public Order(String orderId, Customer customer) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order id must not be blank.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Order needs a customer.");
        }
        this.orderId = orderId.trim();
        this.customer = customer;
    }

    public String orderId() {
        return orderId;
    }

    public Customer customer() {
        return customer;
    }

    public List<OrderItem> items() {
        return List.copyOf(items);
    }

    public OrderStatus status() {
        return status;
    }

    public Payment payment() {
        return payment;
    }

    public Delivery delivery() {
        return delivery;
    }

    public double itemsTotal() {
        double sum = 0;
        for (OrderItem item : items) {
            sum += item.lineTotal();
        }
        return sum;
    }

    /** Only a PENDING order accepts items; stock is reserved immediately. */
    public void addItem(OrderItem item) {
        requireStatus(OrderStatus.PENDING, "add items");
        if (item == null) {
            throw new IllegalArgumentException("Cannot add a null item.");
        }
        item.product().decreaseStock(item.quantity());
        items.add(item);
    }

    public void attachPayment(Payment newPayment) {
        requireStatus(OrderStatus.PENDING, "attach a payment");
        if (newPayment == null) {
            throw new IllegalArgumentException("Cannot attach a null payment.");
        }
        this.payment = newPayment;
    }

    public void attachDelivery(Delivery newDelivery) {
        requireStatus(OrderStatus.PENDING, "attach a delivery");
        if (newDelivery == null) {
            throw new IllegalArgumentException("Cannot attach a null delivery.");
        }
        this.delivery = newDelivery;
    }

    public void confirm() {
        requireStatus(OrderStatus.PENDING, "confirm");
        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot confirm order " + orderId + " with no items.");
        }
        status = OrderStatus.CONFIRMED;
    }

    /** Business rule: nothing ships until the payment is PAID and a delivery exists. */
    public void ship() {
        requireStatus(OrderStatus.CONFIRMED, "ship");
        if (payment == null || !payment.isPaid()) {
            throw new IllegalStateException(
                    "Order " + orderId + " cannot ship until its payment is PAID.");
        }
        if (delivery == null) {
            throw new IllegalStateException(
                    "Order " + orderId + " cannot ship without a delivery.");
        }
        status = OrderStatus.SHIPPED;
        delivery.startTransit();
    }

    public void deliver() {
        requireStatus(OrderStatus.SHIPPED, "deliver");
        status = OrderStatus.DELIVERED;
        delivery.markDelivered();
        customer.earnLoyaltyPoints(10 * items.size());
    }

    /** Cancelling returns reserved stock and refunds a paid payment. */
    public void cancel() {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "A " + status + " order cannot be cancelled.");
        }
        for (OrderItem item : items) {
            item.product().increaseStock(item.quantity());
        }
        if (payment != null && payment.isPaid()) {
            payment.refund();
        }
        status = OrderStatus.CANCELLED;
    }

    /**
     * Bridge into the Week-1 pricing engine: the domain model never re-implements
     * pricing rules, it re-uses {@link store.OrderCalculator} as the single source
     * of truth.
     */
    public store.PriceBreakdown priceBreakdown(store.OrderCalculator calculator, String promoCode) {
        List<store.OrderItem> lines = new ArrayList<>();
        for (OrderItem item : items) {
            lines.add(new store.OrderItem(
                    item.product().name(), item.product().unitPrice(), item.quantity()));
        }
        store.DeliveryZone zone = delivery != null
                ? delivery.destination().zone()
                : customer.address().zone();
        store.Order pricingOrder =
                new store.Order(lines, promoCode, customer.type(), zone);
        return calculator.calculate(pricingOrder);
    }

    private void requireStatus(OrderStatus needed, String action) {
        if (status != needed) {
            throw new IllegalStateException("Cannot " + action + " when order "
                    + orderId + " is " + status + " (needs " + needed + ").");
        }
    }
}
