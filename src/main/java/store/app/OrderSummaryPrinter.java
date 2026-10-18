package store.app;

import java.util.List;
import java.util.Locale;

import store.model.DeliveryZone;
import store.model.PriceBreakdown;
import store.model.PricingLineItem;
import store.util.PricingConfig;
import store.util.PricingFormat;

/** One receipt formatter shared by the manual and catalog ordering flows. */
public final class OrderSummaryPrinter {
    private OrderSummaryPrinter() { }

    public static void print(List<PricingLineItem> items, DeliveryZone zone, PriceBreakdown breakdown) {
        System.out.println();
        System.out.println("------------------- Order summary -------------------");
        for (PricingLineItem item : items) {
            System.out.println(String.format(Locale.US, " %2d x %-24s @ %15s = %15s",
                    item.quantity(), shortName(item.name()),
                    PricingFormat.money(item.unitPrice()), PricingFormat.money(item.lineTotal())));
        }
        System.out.println();
        System.out.println(row("Subtotal", PricingFormat.money(breakdown.subtotal())));
        if (breakdown.discountAmount() > 0) {
            System.out.println(row("Discount ("
                    + String.format(Locale.US, "%.0f", breakdown.discountRate() * 100) + "%)",
                    "-" + PricingFormat.money(breakdown.discountAmount())));
        }
        System.out.println(row("Discounted subtotal", PricingFormat.money(breakdown.discountedSubtotal())));
        System.out.println(row("Delivery fee (" + zone.label() + ")",
                breakdown.deliveryFee() == 0 ? "FREE" : PricingFormat.money(breakdown.deliveryFee())));
        System.out.println(row("VAT ("
                + String.format(Locale.US, "%.2f", PricingConfig.TAX_RATE * 100) + "%)",
                PricingFormat.money(breakdown.taxAmount())));
        System.out.println("------------------------------------------------------");
        System.out.println(row("TOTAL PAYABLE", PricingFormat.money(breakdown.total())));

        if (!breakdown.notes().isEmpty()) {
            System.out.println();
            System.out.println("Rules applied:");
            for (String note : breakdown.notes()) {
                System.out.println("  * " + note);
            }
        }
    }

    private static String shortName(String name) {
        return name.length() > 24 ? name.substring(0, 21) + "..." : name;
    }

    private static String row(String label, String value) {
        return String.format(Locale.US, " %-28s %15s", label + ":", value);
    }
}
