package store.repository;

import java.util.Optional;

import store.model.Order;

/** Persistence boundary for order aggregates. */
public interface OrderRepository {
    Optional<Order> findById(String orderId);

    void save(Order order);
}
