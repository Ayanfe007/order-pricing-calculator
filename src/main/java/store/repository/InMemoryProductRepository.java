package store.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import store.model.Product;
import store.exception.ValidationException;

/** Deterministic product repository for the console app and tests. */
public final class InMemoryProductRepository implements ProductRepository {
    private final Map<String, Product> products = new LinkedHashMap<>();

    public InMemoryProductRepository() { }

    public InMemoryProductRepository(List<Product> initialProducts) {
        if (initialProducts != null) {
            initialProducts.forEach(this::save);
        }
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(products.values());
    }

    @Override
    public void save(Product product) {
        if (product == null) {
            throw new ValidationException("Cannot save a null product.");
        }
        products.put(product.sku(), product);
    }
}
