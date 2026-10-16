package store.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import store.model.CustomerType;
import store.model.DeliveryZone;
import store.model.PriceBreakdown;
import store.model.PricingLineItem;
import store.model.PricingOrder;
import store.model.Product;
import store.repository.InMemoryProductRepository;
import store.repository.ProductRepository;
import store.service.OrderPricingService;
import store.util.PricingFormat;

/** Catalog front end: prices and stock come from a repository, never from input. */
public final class CatalogApp {
    private CatalogApp() { }

    public static void main(String[] args) {
        ProductRepository products = new InMemoryProductRepository(List.of(
                new Product("GC-001", "Golden Rice 5kg", 12000, 10),
                new Product("GC-002", "Groundnut Oil 1L", 4500, 8),
                new Product("JP-003", "Jollof Party Pack", 15000, 6),
                new Product("ZB-004", "Zobo Drink (bottle)", 500, 24),
                new Product("BR-005", "Bread (loaf)", 590, 15)));
        System.out.println("======================================================");
        System.out.println(" Online Store - Catalog Ordering (prices & stock shown)");
        System.out.println("======================================================");
        try {
            run(new Scanner(System.in), products);
        } catch (NoSuchElementException e) {
            System.out.println("Error: input ended before the order was complete.");
        }
    }

    static void run(Scanner scanner, ProductRepository products) {
        ConsoleInput input = new ConsoleInput(scanner);
        List<Product> catalog = products.findAll();
        Map<Product, Integer> basket = new LinkedHashMap<>();
        while (true) {
            printCatalog(catalog, basket);
            String pick = input.line("Item number to add (blank or 0 to finish): ").trim();
            if (pick.isEmpty() || pick.equals("0")) {
                break;
            }
            int index;
            try {
                index = ConsoleInput.parseInteger(pick, "item number");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                continue;
            }
            if (index < 1 || index > catalog.size()) {
                System.out.println("Error: there is no item number " + index
                        + ". Choose 1-" + catalog.size() + ".");
                continue;
            }
            addPickedProduct(input, catalog.get(index - 1), basket);
        }

        if (basket.isEmpty()) {
            System.out.println("No items picked - nothing to price.");
            return;
        }
        priceBasket(input, basket);
    }

    private static void addPickedProduct(ConsoleInput input, Product product,
                                         Map<Product, Integer> basket) {
        int left = product.stock() - basket.getOrDefault(product, 0);
        if (left < 1) {
            System.out.println("Error: " + product.name() + " is out of stock.");
            return;
        }
        String quantityText = input.line("Quantity of " + product.name()
                + " (stock left " + left + "): ").trim();
        int quantity;
        try {
            quantity = ConsoleInput.parseInteger(quantityText, "quantity");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
        if (quantity < 1) {
            System.out.println("Error: quantity must be at least 1, got " + quantity + ".");
        } else if (quantity > left) {
            System.out.println("Error: only " + left + " of " + product.name() + " left in stock.");
        } else {
            basket.merge(product, quantity, Integer::sum);
            System.out.println("Added " + quantity + " x " + product.name() + ".");
        }
    }

    private static void priceBasket(ConsoleInput input, Map<Product, Integer> basket) {
        CustomerType customer = input.readCustomerType(
                "Customer type (standard / premium / student) [standard]: ");
        String promo = input.readParsed("Promo code (press Enter for none): ",
                raw -> raw.trim().toUpperCase(Locale.US));
        DeliveryZone zone = input.readDeliveryZone("Delivery zone (A / B / C): ");
        List<PricingLineItem> lines = pricingLines(basket);
        PriceBreakdown breakdown = new OrderPricingService().calculate(
                new PricingOrder(lines, promo, customer, zone));
        OrderSummaryPrinter.print(lines, zone, breakdown);
        printRemainingStock(basket);
    }

    private static List<PricingLineItem> pricingLines(Map<Product, Integer> basket) {
        List<PricingLineItem> lines = new ArrayList<>();
        for (Map.Entry<Product, Integer> entry : basket.entrySet()) {
            Product product = entry.getKey();
            lines.add(new PricingLineItem(product.name(), product.unitPrice(), entry.getValue()));
        }
        return lines;
    }

    private static void printRemainingStock(Map<Product, Integer> basket) {
        System.out.println();
        System.out.println("Stock left after this order:");
        for (Map.Entry<Product, Integer> entry : basket.entrySet()) {
            int left = entry.getKey().stock() - entry.getValue();
            System.out.println("  - " + entry.getKey().name() + ": " + left);
        }
    }

    static void printCatalog(List<Product> catalog, Map<Product, Integer> basket) {
        System.out.println();
        System.out.println(" Current catalogue:");
        System.out.println("  #  Product                    Price (NGN)   Stock left");
        for (int i = 0; i < catalog.size(); i++) {
            Product product = catalog.get(i);
            int left = product.stock() - basket.getOrDefault(product, 0);
            System.out.println(String.format(Locale.US, " %2d  %-24s %15s %14d",
                    i + 1, product.name(), PricingFormat.money(product.unitPrice()), left));
        }
    }
}
