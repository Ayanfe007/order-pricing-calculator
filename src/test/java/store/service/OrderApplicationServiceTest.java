package store.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import store.model.CustomerType;
import store.model.DeliveryZone;
import store.model.Address;
import store.model.Customer;
import store.model.DeliveryStatus;
import store.model.Order;
import store.model.OrderStatus;
import store.model.PaymentStatus;
import store.model.Product;
import store.exception.NotFoundException;
import store.repository.InMemoryOrderRepository;
import store.repository.InMemoryProductRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderApplicationServiceTest {
    private static Address address() {
        return new Address("1 Test Way", "Ikeja", "Lagos", DeliveryZone.ZONE_A);
    }

    private static Customer customer() {
        return new Customer("Ada", "ada@example.com", "08031234567",
                address(), CustomerType.STANDARD);
    }

    @Test
    void serviceCoordinatesRepositoriesPaymentAndLifecycle() {
        Product rice = new Product("SKU-1", "Rice", 1_000, 5);
        InMemoryProductRepository products = new InMemoryProductRepository(List.of(rice));
        InMemoryOrderRepository orders = new InMemoryOrderRepository();
        OrderApplicationService service = new OrderApplicationService(
                products, orders, new PaymentProcessingService());

        service.createOrder("ORD-1", customer());
        service.addProduct("ORD-1", "SKU-1", 2);
        service.attachPayment("ORD-1", "TRANSFER", 2_000);
        service.attachDelivery("ORD-1", address());
        service.confirm("ORD-1");
        service.processPayment("ORD-1");
        service.ship("ORD-1");
        service.deliver("ORD-1");

        Order saved = orders.findById("ORD-1").orElseThrow();
        assertEquals(OrderStatus.DELIVERED, saved.status());
        assertEquals(PaymentStatus.PAID, saved.payment().status());
        assertEquals(DeliveryStatus.DELIVERED, saved.delivery().status());
        assertEquals(3, products.findBySku("SKU-1").orElseThrow().stock());
        assertEquals(10, saved.customer().loyaltyPoints());
    }

    @Test
    void serviceReportsMissingProductsBeforeChangingOrder() {
        InMemoryProductRepository products = new InMemoryProductRepository();
        InMemoryOrderRepository orders = new InMemoryOrderRepository();
        OrderApplicationService service = new OrderApplicationService(
                products, orders, new PaymentProcessingService());
        service.createOrder("ORD-1", customer());

        assertThrows(NotFoundException.class, () -> service.addProduct("ORD-1", "MISSING", 1));
        assertEquals(0, orders.findById("ORD-1").orElseThrow().items().size());
    }
}
