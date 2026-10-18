package store.model;

import java.util.ArrayList;
import java.util.List;

import store.exception.StateTransitionException;
import store.exception.ValidationException;
import store.validation.OrderValidator;

/**
 * The order aggregate owns its lifecycle, inventory reservations, refunds,
 * delivery transitions, and customer loyalty side effects.
 */
public final class Order {
    private final String orderId;
    private final Customer customer;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.PENDING;
    private Payment payment;
    private Delivery delivery;

    public Order(String orderId, Customer customer) {
        OrderValidator.validateDomainOrder(orderId, customer != null);
        this.orderId = orderId.trim();
        this.customer = customer;
    }

    public String orderId() { return orderId; }
    public Customer customer() { return customer; }
    public List<OrderItem> items() { return List.copyOf(items); }
    public OrderStatus status() { return status; }
    public Payment payment() { return payment; }
    public Delivery delivery() { return delivery; }

    public double itemsTotal() {
        double sum = 0.0;
        for (OrderItem item : items) {
            sum += item.lineTotal();
        }
        return sum;
    }

    /** Only a PENDING order accepts items; stock is reserved immediately. */
    public void addItem(OrderItem item) {
        requireStatus(OrderStatus.PENDING, "add items");
        if (item == null) {
            throw new ValidationException("Cannot add a null item.");
        }
        item.product().decreaseStock(item.quantity());
        items.add(item);
    }

    public void attachPayment(Payment newPayment) {
        requireStatus(OrderStatus.PENDING, "attach a payment");
        if (newPayment == null) {
            throw new ValidationException("Cannot attach a null payment.");
        }
        payment = newPayment;
    }

    public void attachDelivery(Delivery newDelivery) {
        requireStatus(OrderStatus.PENDING, "attach a delivery");
        if (newDelivery == null) {
            throw new ValidationException("Cannot attach a null delivery.");
        }
        delivery = newDelivery;
    }

    public void confirm() {
        requireStatus(OrderStatus.PENDING, "confirm");
        if (items.isEmpty()) {
            throw new StateTransitionException(
                    "Cannot confirm order " + orderId + " with no items.");
        }
        status = OrderStatus.CONFIRMED;
    }

    /** No shipment is started unless payment is PAID and delivery exists. */
    public void ship() {
        requireStatus(OrderStatus.CONFIRMED, "ship");
        if (payment == null || !payment.isPaid()) {
            throw new StateTransitionException(
                    "Order " + orderId + " cannot ship until its payment is PAID.");
        }
        if (delivery == null) {
            throw new StateTransitionException(
                    "Order " + orderId + " cannot ship without a delivery.");
        }
        delivery.startTransit();
        status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        requireStatus(OrderStatus.SHIPPED, "deliver");
        delivery.markDelivered();
        customer.earnLoyaltyPoints(10 * items.size());
        status = OrderStatus.DELIVERED;
    }

    /** Cancelling returns reserved stock and refunds a paid payment. */
    public void cancel() {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new StateTransitionException("A " + status + " order cannot be cancelled.");
        }
        for (OrderItem item : items) {
            item.product().increaseStock(item.quantity());
        }
        if (payment != null && payment.isPaid()) {
            payment.refund();
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus needed, String action) {
        if (status != needed) {
            throw new StateTransitionException("Cannot " + action + " when order "
                    + orderId + " is " + status + " (needs " + needed + ").");
        }
    }
}
