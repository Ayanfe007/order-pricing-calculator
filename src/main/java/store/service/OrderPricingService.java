package store.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import store.CustomerType;
import store.DeliveryZone;
import store.Order;
import store.OrderItem;
import store.PriceBreakdown;
import store.PricingConfig;
import store.util.Money;
import store.util.PricingFormat;

/**
 * Pure pricing use case. It composes independent discount and shipping
 * strategies, then applies VAT and assembles a result; it performs no I/O.
 */
public final class OrderPricingService {
    private final DiscountPolicy discountPolicy;
    private final ShippingCalculator shippingCalculator;

    public OrderPricingService() {
        this(defaultDiscountPolicy(), new ThresholdShippingCalculator(
                new ZoneShippingCalculator(), new FreeShippingCalculator()));
    }

    public OrderPricingService(DiscountPolicy discountPolicy,
                               ShippingCalculator shippingCalculator) {
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "discountPolicy");
        this.shippingCalculator = Objects.requireNonNull(shippingCalculator, "shippingCalculator");
    }

    private static DiscountPolicy defaultDiscountPolicy() {
        return new CompositeDiscountPolicy(List.of(
                new TieredDiscountPolicy(),
                new PromoCodeDiscountPolicy(),
                new CustomerTypeDiscountPolicy()), PricingConfig.MAX_DISCOUNT_RATE);
    }

    /** Sum of line totals, rounded to two decimal places. */
    public double subtotal(Order order) {
        double sum = 0.0;
        for (OrderItem item : order.items()) {
            sum += item.lineTotal();
        }
        return Money.round2(sum);
    }

    /** Kept as a small query for the original inclusive tier rule. */
    public double tieredRate(double subtotal) {
        return new TieredDiscountPolicy().rateFor(subtotal);
    }

    /** Kept as a query for callers that need the promo's percentage component. */
    public double promoRate(String promoCode) {
        return new PromoCodeDiscountPolicy().rateFor(promoCode);
    }

    public double discountRate(Order order, double subtotal) {
        return discountPolicy.evaluate(order, subtotal).rate();
    }

    public boolean deliveryWaived(Order order, double discountedSubtotal) {
        return shippingCalculator.calculate(order, discountedSubtotal).fee() == 0.0;
    }

    public double deliveryFee(Order order, double discountedSubtotal) {
        return shippingCalculator.calculate(order, discountedSubtotal).fee();
    }

    /** VAT remains on discounted goods only; delivery is not taxable. */
    public double tax(double discountedSubtotal) {
        return Money.round2(discountedSubtotal * PricingConfig.TAX_RATE);
    }

    /** Prices a validated Week-1 pricing order without console or repository access. */
    public PriceBreakdown calculate(Order order) {
        Objects.requireNonNull(order, "order");
        double subtotal = subtotal(order);
        DiscountQuote discount = discountPolicy.evaluate(order, subtotal);
        double discountAmount = Money.round2(subtotal * discount.rate());
        double discountedSubtotal = Money.round2(subtotal - discountAmount);
        ShippingQuote shipping = shippingCalculator.calculate(order, discountedSubtotal);
        List<String> notes = new ArrayList<>(discount.notes());
        if (shipping.waivedByThreshold()) {
            notes.add("Free delivery: discounted order value of "
                    + PricingFormat.money(discountedSubtotal) + " is above "
                    + PricingFormat.money(PricingConfig.FREE_DELIVERY_THRESHOLD) + ".");
        }
        double taxAmount = tax(discountedSubtotal);
        double total = Money.round2(discountedSubtotal + shipping.fee() + taxAmount);
        return new PriceBreakdown(subtotal, discount.rate(), discountAmount,
                discountedSubtotal, shipping.fee(), taxAmount, total, notes);
    }

    /**
     * Prices the live Week-2 aggregate by adapting its product-backed lines to
     * the Week-1 pricing request. Mapping lives in the service layer, not model.
     */
    public PriceBreakdown calculate(store.domain.Order order, String promoCode) {
        Objects.requireNonNull(order, "order");
        List<OrderItem> lines = new ArrayList<>();
        for (store.domain.OrderItem item : order.items()) {
            lines.add(new OrderItem(item.product().name(),
                    item.product().unitPrice(), item.quantity()));
        }
        DeliveryZone zone = order.delivery() != null
                ? order.delivery().destination().zone()
                : order.customer().address().zone();
        CustomerType customerType = order.customer().type();
        return calculate(new Order(lines, promoCode, customerType, zone));
    }

    /** Compatibility helper retained for the original test suite. */
    public static double round2(double amount) {
        return Money.round2(amount);
    }
}
