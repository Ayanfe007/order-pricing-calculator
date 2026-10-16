package store.domain;

import store.exception.InsufficientStockException;
import store.exception.StateTransitionException;
import store.exception.ValidationException;
import store.validation.ProductValidator;

/**
 * A class because stock changes over time. Stock can only move through the
 * two guarded methods, so it can never go negative.
 */
public final class Product {

    private final String sku;
    private final String name;
    private final double unitPrice;
    private int stock;

    public Product(String sku, String name, double unitPrice, int stock) {
        ProductValidator.validate(sku, name, unitPrice, stock);
        this.sku = sku.trim();
        this.name = name.trim();
        this.unitPrice = unitPrice;
        this.stock = stock;
    }

    public String sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public double unitPrice() {
        return unitPrice;
    }

    public int stock() {
        return stock;
    }

    /** Reserves stock for an order; refuses overselling. */
    public void decreaseStock(int qty) {
        requirePositive(qty);
        if (qty > stock) {
            throw new InsufficientStockException("Not enough stock of " + name
                    + ": requested " + qty + ", available " + stock + ".");
        }
        stock -= qty;
    }

    /** Returns reserved stock (e.g. when an order is cancelled). */
    public void increaseStock(int qty) {
        requirePositive(qty);
        stock += qty;
    }

    private static void requirePositive(int qty) {
        if (qty < 1) {
            throw new ValidationException("Quantity must be at least 1, got " + qty + ".");
        }
    }
}
