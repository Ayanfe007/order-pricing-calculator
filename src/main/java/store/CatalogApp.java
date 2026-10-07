package store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import store.domain.Product;

/**
 * Catalog ordering front-end. The store's products - with their prices and
 * live stock - are printed, and the customer picks items by number and
 * quantity. Prices are never typed by the customer; stock can never be
 * oversold. Uses the Week-2 {@link Product} for stock and the Week-1
 * {@link OrderCalculator} for the money maths.
 */
public final class CatalogApp {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Product> catalog = new ArrayList<>(List.of(
                new Product("GC-001", "Golden Rice 5kg", 12000, 10),
                new Product("GC-002", "Groundnut Oil 1L", 4500, 8),
                new Product("JP-003", "Jollof Party Pack", 15000, 6),
                new Product("ZB-004", "Zobo Drink (bottle)", 500, 24),
                new Product("BR-005", "Bread (loaf)", 590, 15)));
        System.out.println("======================================================");
        System.out.println(" Online Store - Catalog Ordering (prices & stock shown)");
        System.out.println("======================================================");
        try {
            run(in, catalog);
        } catch (NoSuchElementException e) {
            System.out.println("Error: input ended before the order was complete.");
        }
    }

    static void run(Scanner in, List<Product> catalog) {
        Map<Product, Integer> basket = new LinkedHashMap<>();

        while (true) {
            printCatalog(catalog, basket);
            String pick = prompt(in, "Item number to add (blank or 0 to finish): ").trim();
            if (pick.isEmpty() || pick.equals("0")) {
                break;
            }
            int idx;
            try {
                idx = Integer.parseInt(pick);
            } catch (NumberFormatException e) {
                System.out.println("Error: item number must be a whole number, got '" + pick + "'.");
                continue;
            }
            if (idx < 1 || idx > catalog.size()) {
                System.out.println("Error: there is no item number " + idx
                        + ". Choose 1-" + catalog.size() + ".");
                continue;
            }
            Product product = catalog.get(idx - 1);
            int already = basket.getOrDefault(product, 0);
            int left = product.stock() - already;
            if (left < 1) {
                System.out.println("Error: " + product.name() + " is out of stock.");
                continue;
            }
            String qtyText = prompt(in, "Quantity of " + product.name()
                    + " (stock left " + left + "): ").trim();
            int qty;
            try {
                qty = Integer.parseInt(qtyText);
            } catch (NumberFormatException e) {
                System.out.println("Error: quantity must be a whole number, got '" + qtyText + "'.");
                continue;
            }
            if (qty < 1) {
                System.out.println("Error: quantity must be at least 1, got " + qty + ".");
            } else if (qty > left) {
                System.out.println("Error: only " + left + " of " + product.name() + " left in stock.");
            } else {
                basket.merge(product, qty, Integer::sum);
                System.out.println("Added " + qty + " x " + product.name() + ".");
            }
        }

        if (basket.isEmpty()) {
            System.out.println("No items picked - nothing to price.");
            return;
        }

        CustomerType customer = readCustomer(in);
        String promo = prompt(in, "Promo code (press Enter for none): ").trim().toUpperCase(Locale.US);
        DeliveryZone zone = readZone(in);

        List<OrderItem> lines = new ArrayList<>();
        for (Map.Entry<Product, Integer> e : basket.entrySet()) {
            lines.add(new OrderItem(e.getKey().name(), e.getKey().unitPrice(), e.getValue()));
        }
        PriceBreakdown bd = new OrderCalculator()
                .calculate(new Order(lines, promo, customer, zone));

        System.out.println();
        System.out.println("------------------- Order summary -------------------");
        for (Map.Entry<Product, Integer> e : basket.entrySet()) {
            System.out.println(String.format(Locale.US, " %2d x %-24s @ %15s = %15s",
                    e.getValue(), e.getKey().name(),
                    money(e.getKey().unitPrice()), money(e.getValue() * e.getKey().unitPrice())));
        }
        System.out.println();
        System.out.println(row("Subtotal", money(bd.subtotal())));
        if (bd.discountAmount() > 0) {
            System.out.println(row("Discount ("
                    + String.format(Locale.US, "%.0f", bd.discountRate() * 100) + "%)",
                    "-" + money(bd.discountAmount())));
        }
        System.out.println(row("Discounted subtotal", money(bd.discountedSubtotal())));
        System.out.println(row("Delivery fee (" + zone.label() + ")",
                bd.deliveryFee() == 0 ? "FREE" : money(bd.deliveryFee())));
        System.out.println(row("VAT (7.50%)", money(bd.taxAmount())));
        System.out.println("------------------------------------------------------");
        System.out.println(row("TOTAL PAYABLE", money(bd.total())));
        if (!bd.notes().isEmpty()) {
            System.out.println();
            System.out.println("Rules applied:");
            for (String note : bd.notes()) {
                System.out.println("  * " + note);
            }
        }
        System.out.println();
        System.out.println("Stock left after this order:");
        for (Map.Entry<Product, Integer> e : basket.entrySet()) {
            System.out.println("  - " + e.getKey().name() + ": "
                    + (e.getKey().stock() - e.getValue()));
        }
    }

    static void printCatalog(List<Product> catalog, Map<Product, Integer> basket) {
        System.out.println();
        System.out.println(" Current catalogue:");
        System.out.println("  #  Product                    Price (NGN)   Stock left");
        for (int i = 0; i < catalog.size(); i++) {
            Product p = catalog.get(i);
            int left = p.stock() - basket.getOrDefault(p, 0);
            System.out.println(String.format(Locale.US, " %2d  %-24s %15s %14d",
                    i + 1, p.name(), money(p.unitPrice()), left));
        }
    }

    static CustomerType readCustomer(Scanner in) {
        while (true) {
            String raw = prompt(in,
                    "Customer type (standard / premium / student) [standard]: ").trim();
            try {
                return raw.isEmpty() ? CustomerType.STANDARD : CustomerType.fromInput(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    static DeliveryZone readZone(Scanner in) {
        while (true) {
            try {
                return DeliveryZone.fromInput(prompt(in, "Delivery zone (A / B / C): "));
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    static String prompt(Scanner in, String text) {
        System.out.print(text);
        return in.nextLine();
    }

    static String row(String label, String value) {
        return String.format(Locale.US, " %-28s %15s", label + ":", value);
    }

    static String money(double amount) {
        return String.format(Locale.US, "%s %,.2f", PricingConfig.CURRENCY, amount);
    }
}
