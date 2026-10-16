package store.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import store.model.CustomerType;
import store.model.DeliveryZone;
import store.model.PriceBreakdown;
import store.model.PricingLineItem;
import store.model.PricingOrder;
import store.service.OrderPricingService;
import store.util.PricingConfig;

/** Console entry point; parsing, pricing, and receipt formatting stay separate. */
public final class ConsoleApp {
    private ConsoleApp() { }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ConsoleInput input = new ConsoleInput(scanner);
        System.out.println("======================================================");
        System.out.println(" Online Store - Order Pricing & Delivery Calculator");
        System.out.println("======================================================");
        try {
            PricingOrder order = readOrder(input);
            PriceBreakdown breakdown = new OrderPricingService().calculate(order);
            OrderSummaryPrinter.print(order.items(), order.zone(), breakdown);
        } catch (NoSuchElementException e) {
            System.out.println("Error: input ended before the order was complete.");
        }
    }

    static PricingOrder readOrder(ConsoleInput input) {
        int itemCount = input.readInt("How many different items are on the order? ",
                1, 50, "number of items");
        List<PricingLineItem> items = new ArrayList<>();
        for (int i = 1; i <= itemCount; i++) {
            System.out.println("--- Item " + i + " of " + itemCount + " ---");
            String name = input.readParsed("Item name: ", raw -> {
                if (raw.trim().isEmpty()) {
                    throw new IllegalArgumentException("Item name must not be empty.");
                }
                return raw.trim();
            });
            double price = input.readPositiveAmount(
                    "Unit price in " + PricingConfig.CURRENCY + ": ");
            int quantity = input.readInt("Quantity: ", 1, PricingLineItem.MAX_QUANTITY, "quantity");
            items.add(new PricingLineItem(name, price, quantity));
        }

        CustomerType customer = input.readCustomerType(
                "Customer type (standard / premium / student) [standard]: ");
        String promo = input.readParsed("Promo code (press Enter for none): ",
                raw -> raw.trim().toUpperCase(Locale.US));
        DeliveryZone zone = input.readDeliveryZone("Delivery zone (A / B / C): ");
        return new PricingOrder(items, promo, customer, zone);
    }
}
