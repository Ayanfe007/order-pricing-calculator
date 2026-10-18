package store.util;

/** Small monetary operations shared by pricing and presentation code. */
public final class Money {
    private Money() { }

    /** Rounds to two decimal places (kobo), preserving the project's Week-1 rule. */
    public static double round2(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
