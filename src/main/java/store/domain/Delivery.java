package store.domain;

/**
 * A class with a guarded state machine:
 * PREPARING -> IN_TRANSIT -> DELIVERED (or FAILED while in transit).
 * The fee comes from the destination's Week-1 delivery zone.
 */
public final class Delivery {

    private final Address destination;
    private DeliveryStatus status = DeliveryStatus.PREPARING;

    public Delivery(Address destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Delivery needs a destination address.");
        }
        this.destination = destination;
    }

    public Address destination() {
        return destination;
    }

    public DeliveryStatus status() {
        return status;
    }

    /** Location-based fee re-used from the Week-1 zone table. */
    public double fee() {
        return destination.zone().baseFee();
    }

    public void startTransit() {
        if (status != DeliveryStatus.PREPARING) {
            throw new IllegalStateException(
                    "Delivery can leave PREPARING only once; it is " + status + ".");
        }
        status = DeliveryStatus.IN_TRANSIT;
    }

    public void markDelivered() {
        if (status != DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                    "Only an IN_TRANSIT delivery can be delivered; it is " + status + ".");
        }
        status = DeliveryStatus.DELIVERED;
    }

    public void markFailed() {
        if (status != DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                    "Only an IN_TRANSIT delivery can fail; it is " + status + ".");
        }
        status = DeliveryStatus.FAILED;
    }
}
