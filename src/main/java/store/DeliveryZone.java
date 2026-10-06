package store;

/**
 * Delivery zones with their base fees (location-based rule).
 * The order-value rule (free delivery over a threshold) lives in
 * {@link OrderCalculator}.
 */
public enum DeliveryZone {

    ZONE_A("Zone A - Lagos Island", 1_500.0),
    ZONE_B("Zone B - Lagos Mainland", 2_500.0),
    ZONE_C("Zone C - Outside Lagos", 5_000.0);

    private final String label;
    private final double baseFee;

    DeliveryZone(String label, double baseFee) {
        this.label = label;
        this.baseFee = baseFee;
    }

    public String label() {
        return label;
    }

    public double baseFee() {
        return baseFee;
    }

    /** Accepts "A", "a", "ZONE_A", "zone b", ... and rejects anything else. */
    public static DeliveryZone fromInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Delivery zone must not be empty. Valid zones: A, B, C.");
        }
        String norm = input.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        if (!norm.startsWith("ZONE_")) {
            norm = "ZONE_" + norm;
        }
        for (DeliveryZone zone : values()) {
            if (zone.name().equals(norm)) {
                return zone;
            }
        }
        throw new IllegalArgumentException(
                "Unknown delivery zone '" + input.trim() + "'. Valid zones: A, B or C.");
    }
}
