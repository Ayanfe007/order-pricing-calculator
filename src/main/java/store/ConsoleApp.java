package store;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Console front-end for the pricing engine.
 *
 * This class only collects input, validates the raw text and prints results.
 * Every pricing rule lives in {@link OrderCalculator} / {@link PricingConfig},
 * which is what keeps the business logic testable without a keyboard.
 */
public final class ConsoleApp {

    @FunctionalInterface
    private interface TextParser<T> {
        T parse(String raw);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("======================================================");
        System.out.println(" Online Store - Order Pricing & Delivery Calculator");
        System.out.println("======================================================");
        try {
            Order order = readOrder(in);
            printBreakdown(order, new OrderCalculator().calculate(order));
        } catch (NoSuchElementException e) {
            System.out.println("Error: input ended before the order was complete.");
        }
    }

    // ------------------------------------------------------------------
    // input
    // ------------------------------------------------------------------

    static Order readOrder(Scanner in) {
        int itemCount = readInt(in, "How many different items are on the order? ",
                1, 50, "number of items");

        List<OrderItem> items = new ArrayList<>();
        for (int i = 1; i <= itemCount; i++) {
            System.out.println("--- Item " + i + " of " + itemCount + " ---");
            String name = readParsed(in, "Item name: ", raw -> {
                if (raw.trim().isEmpty()) {
                    throw new IllegalArgumentException("Item name must not be empty.");
                }
                return raw.trim();
            });
            double price = readPositiveAmount(in,
                    "Unit price in " + PricingConfig.CURRENCY + ": ");
            int qty = readInt(in, "Quantity: ", 1, OrderItem.MAX_QUANTITY, "quantity");
            items.add(new OrderItem(name, price, qty));
        }

        CustomerType customer = readParsed(in,
                "Customer type (standard / premium / student) [standard]: ",
                raw -> raw.trim().isEmpty() ? CustomerType.STANDARD : CustomerType.fromInput(raw));

        String promo = readParsed(in, "Promo code (press Enter for none): ",
                raw -> raw.trim()).toUpperCase(Locale.US);

        DeliveryZone zone = readParsed(in, "Delivery zone (A / B / C): ",
                DeliveryZone::fromInput);

        return new Order(items, promo, customer, zone);
    }

    private static String readLine(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }

    /** Prompts until the parser accepts the answer; prints clear errors meanwhile. */
    private static <T> T readParsed(Scanner in, String prompt, TextParser<T> parser) {
        while (true) {
            String raw = readLine(in, prompt);
            try {
                return parser.parse(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static int readInt(Scanner in, String prompt, int min, int max, String what) {
        return readParsed(in, prompt, raw -> {
            String text = raw.trim();
            int value;
            try {
                value = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(capital(what)
                        + " must be a whole number, got '" + text + "'.");
            }
            if (value < min || value > max) {
                throw new IllegalArgumentException(capital(what) + " must be between "
                        + min + " and " + max + ", got " + value + ".");
            }
            return value;
        });
    }

    private static double readPositiveAmount(Scanner in, String prompt) {
        return readParsed(in, prompt, raw -> {
            String text = raw.trim();
            double value;
            try {
                value = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price must be a number, got '" + text + "'.");
            }
            if (Double.isNaN(value) || value <= 0) {
                throw new IllegalArgumentException("Price must be a positive amount, got "
                        + text + ".");
            }
            return value;
        });
    }

    private static String capital(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ------------------------------------------------------------------
    // output
    // ------------------------------------------------------------------

    static void printBreakdown(Order order, PriceBreakdown bd) {
        System.out.println();
        System.out.println("------------------- Order summary -------------------");
        for (OrderItem item : order.items()) {
            System.out.println(String.format(Locale.US, " %2d x %-24s @ %15s = %15s",
                    item.quantity(), shortName(item.name()),
                    money(item.unitPrice()), money(item.lineTotal())));
        }
        System.out.println();
        System.out.println(row("Subtotal", money(bd.subtotal())));
        if (bd.discountAmount() > 0) {
            System.out.println(row("Discount ("
                    + String.format(Locale.US, "%.0f", bd.discountRate() * 100) + "%)",
                    "-" + money(bd.discountAmount())));
        }
        System.out.println(row("Discounted subtotal", money(bd.discountedSubtotal())));
        System.out.println(row("Delivery fee (" + order.zone().label() + ")",
                bd.deliveryFee() == 0 ? "FREE" : money(bd.deliveryFee())));
        System.out.println(row("VAT ("
                + String.format(Locale.US, "%.2f", PricingConfig.TAX_RATE * 100) + "%)",
                money(bd.taxAmount())));
        System.out.println("------------------------------------------------------");
        System.out.println(row("TOTAL PAYABLE", money(bd.total())));

        if (!bd.notes().isEmpty()) {
            System.out.println();
            System.out.println("Rules applied:");
            for (String note : bd.notes()) {
                System.out.println("  * " + note);
            }
        }
    }

    private static String shortName(String s) {
        return s.length() > 24 ? s.substring(0, 21) + "..." : s;
    }

    private static String row(String label, String value) {
        return String.format(Locale.US, " %-28s %15s", label + ":", value);
    }

    private static String money(double amount) {
        return String.format(Locale.US, "%s %,.2f", PricingConfig.CURRENCY, amount);
    }
}
