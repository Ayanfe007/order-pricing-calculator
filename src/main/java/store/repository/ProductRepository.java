package store.repository;

import java.util.List;
import java.util.Optional;

import store.model.Product;

/** Persistence boundary for products. Implementations can be in-memory or database-backed. */
public interface ProductRepository {
    Optional<Product> findBySku(String sku);

    List<Product> findAll();

    void save(Product product);
}
