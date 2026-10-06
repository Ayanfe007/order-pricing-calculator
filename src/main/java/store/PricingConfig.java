package store;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Every business rule used by the calculator lives here, in one place.
 * The console front-end and the JUnit tests both read these values, so
 * changing a rule here changes the behaviour everywhere.
 */
public final class PricingConfig {

    /** Currency label used when amounts are printed. */
    public static final String CURRENCY = "NGN";

    /* Rule 1 - order-value (volume) discount tiers. */
    public static final double TIER1_THRESHOLD = 50_000.0;   // 5% from here
    public static final double TIER1_RATE      = 0.05;
    public static final double TIER2_THRESHOLD = 100_000.0;  // 10% from here
    public static final double TIER2_RATE      = 0.10;

    /* Rule 2 - promo codes. */
    public static final Map<String, Double> PROMO_CODES;
    static {
        Map<String, Double> codes = new HashMap<>();
        codes.put("WELCOME10", 0.10);
        codes.put("VIP20", 0.20);
        PROMO_CODES = Collections.unmodifiableMap(codes);
    }
    /** Promo code that waives the delivery fee instead of giving a discount. */
    public static final String PROMO_FREE_DELIVERY = "FREESHIP";

    /* Rule 3 - customer-type discount. */
    public static final double PREMIUM_RATE = 0.05;
    public static final double STUDENT_RATE = 0.05;

    /** Percentage discounts never add up to more than this. */
    public static final double MAX_DISCOUNT_RATE = 0.25;

    /* Delivery rules. */
    public static final double FREE_DELIVERY_THRESHOLD = 150_000.0;

    /** VAT charged on the discounted goods amount (delivery is not taxed). */
    public static final double TAX_RATE = 0.075; // 7.5%

    private PricingConfig() {
        // constants only - not instantiable
    }
}
