package store;

/** Customer categories; PREMIUM and STUDENT carry a percentage discount. */
public enum CustomerType {

    STANDARD("Standard customer", 0.0),
    PREMIUM("Premium member", PricingConfig.PREMIUM_RATE),
    STUDENT("Student", PricingConfig.STUDENT_RATE);

    private final String label;
    private final double rate;

    CustomerType(String label, double rate) {
        this.label = label;
        this.rate = rate;
    }

    public String label() {
        return label;
    }

    /** Percentage discount for this customer type (0.05 = 5%). */
    public double rate() {
        return rate;
    }

    public static CustomerType fromInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Customer type must not be empty. Valid types: standard, premium, student.");
        }
        String norm = input.trim().toUpperCase();
        for (CustomerType type : values()) {
            if (type.name().equals(norm)) {
                return type;
            }
        }
        throw new IllegalArgumentException(
                "Unknown customer type '" + input.trim()
                        + "'. Valid types: standard, premium or student.");
    }
}
