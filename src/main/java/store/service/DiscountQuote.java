package store.service;

import java.util.List;

/** Result produced by one or more discount policies. */
public record DiscountQuote(double rate, List<String> notes) {
    public DiscountQuote {
        if (Double.isNaN(rate) || rate < 0.0) {
            throw new IllegalArgumentException("Discount rate cannot be negative or NaN.");
        }
        notes = List.copyOf(notes);
    }
}
