package store.util;

import java.util.Locale;

import store.PricingConfig;

/** Shared formatting for user-facing pricing notes and console summaries. */
public final class PricingFormat {
    private PricingFormat() { }

    public static String money(double amount) {
        return String.format(Locale.US, "%s %,.2f", PricingConfig.CURRENCY, amount);
    }

    public static String percent(double rate) {
        return String.format(Locale.US, "%.0f%%", rate * 100);
    }
}
