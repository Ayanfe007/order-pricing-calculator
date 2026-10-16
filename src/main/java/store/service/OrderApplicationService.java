package store.service;

import java.util.Objects;

import store.domain.Address;
import store.domain.Customer;
import store.domain.Delivery;
import store.domain.Order;
import store.domain.OrderItem;
import store.domain.Payment;
import store.domain.Product;
import store.exception.NotFoundException;
import store.exception.StateTransitionException;
import store.repository.OrderRepository;
import store.repository.ProductRepository;

/**
 * Application use case coordinating an order aggregate with repositories and
 * payment strategies. The model still owns all lifecycle rules and side effects.
 */
public final class OrderApplicationService {
    private final ProductRepository products;
    private final OrderRepository orders;
    private final PaymentProcessingService payments;

    public OrderApplicationService(ProductRepository products,
                                   OrderRepository orders,
                                   PaymentProcessingService payments) {
        this.products = Objects.requireNonNull(products, "products");
        this.orders = Objects.requireNonNull(orders, "orders");
        this.payments = Objects.requireNonNull(payments, "payments");
    }

    public Order createOrder(String orderId, Customer customer) {
        Order order = new Order(orderId, customer);
        orders.save(order);
        return order;
    }

    public void addProduct(String orderId, String sku, int quantity) {
        Order order = requireOrder(orderId);
        Product product = products.findBySku(sku)
                .orElseThrow(() -> new NotFoundException("No product found with SKU '" + sku + "'."));
        order.addItem(new OrderItem(product, quantity));
        products.save(product);
        orders.save(order);
    }

    public void attachPayment(String orderId, String method, double amount) {
        Order order = requireOrder(orderId);
        order.attachPayment(new Payment(method, amount));
        orders.save(order);
    }

    public void attachDelivery(String orderId, Address destination) {
        Order order = requireOrder(orderId);
        order.attachDelivery(new Delivery(destination));
        orders.save(order);
    }

    public void confirm(String orderId) {
        Order order = requireOrder(orderId);
        order.confirm();
        orders.save(order);
    }

    public void processPayment(String orderId) {
        Order order = requireOrder(orderId);
        Payment payment = order.payment();
        if (payment == null) {
            throw new StateTransitionException(
                    "Order " + orderId + " cannot process payment without an attached payment.");
        }
        payments.process(payment);
        orders.save(order);
    }

    public void ship(String orderId) {
        Order order = requireOrder(orderId);
        order.ship();
        orders.save(order);
    }

    public void deliver(String orderId) {
        Order order = requireOrder(orderId);
        order.deliver();
        orders.save(order);
    }

    public void cancel(String orderId) {
        Order order = requireOrder(orderId);
        order.cancel();
        order.items().forEach(item -> products.save(item.product()));
        orders.save(order);
    }

    private Order requireOrder(String orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("No order found with id '" + orderId + "'."));
    }
}
