package store.repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import store.model.Order;
import store.exception.ValidationException;

/** Small in-memory order store; replacing it does not change the service API. */
public final class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> orders = new LinkedHashMap<>();

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public void save(Order order) {
        if (order == null) {
            throw new ValidationException("Cannot save a null order.");
        }
        orders.put(order.orderId(), order);
    }
}
