package com.sathya.restaurant_management.service;

import com.sathya.restaurant_management.dto.OrderDto;
import com.sathya.restaurant_management.dto.OrderItemDto;
import com.sathya.restaurant_management.dto.OrderRequest;
import com.sathya.restaurant_management.exception.ConflictException;
import com.sathya.restaurant_management.exception.ResourceNotFoundException;
import com.sathya.restaurant_management.model.Counter;
import com.sathya.restaurant_management.model.Customer;
import com.sathya.restaurant_management.model.OrderStatus;
import com.sathya.restaurant_management.model.Product;
import com.sathya.restaurant_management.model.SalesOrder;
import com.sathya.restaurant_management.model.SalesOrderItem;
import com.sathya.restaurant_management.repository.CounterRepository;
import com.sathya.restaurant_management.repository.CustomerRepository;
import com.sathya.restaurant_management.repository.ProductRepository;
import com.sathya.restaurant_management.repository.SalesOrderRepository;
import com.sathya.restaurant_management.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    /** Must match PAYMENT_METHODS in the frontend's seedData.js. */
    private static final Set<String> PAYMENT_METHODS = Set.of("cash", "aba", "acleda", "wing", "bakong", "card");

    private final SalesOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CounterRepository counterRepository;

    public List<OrderDto> findAll() {
        return orderRepository.findAllByOrderByNumberDesc().stream().map(this::toDto).toList();
    }

    public OrderDto getById(String id) {
        return toDto(find(id));
    }

    @Transactional
    public OrderDto create(OrderRequest request) {
        String customerId = request.customerId() == null || request.customerId().isBlank()
                ? Customer.WALK_IN_ID : request.customerId();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Customer", customerId));

        // Merge duplicate lines of the same product, keep first-seen order.
        Map<String, Integer> qtyByProduct = new LinkedHashMap<>();
        for (OrderRequest.Line line : request.items()) {
            qtyByProduct.merge(line.productId(), line.qty(), Integer::sum);
        }

        SalesOrder order = new SalesOrder();
        order.setId(Ids.next("o"));
        order.setNumber(nextOrderNumber());
        order.setInvoiceCode(Ids.invoiceCode());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> e : qtyByProduct.entrySet()) {
            Product product = productRepository.findById(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "A product in your cart no longer exists. Please refresh the menu."));
            if (e.getValue() > product.getStock()) {
                throw new ConflictException(product.getStock() <= 0
                        ? product.getName() + " is out of stock."
                        : "Only " + product.getStock() + " left of " + product.getName() + ".");
            }
            SalesOrderItem item = new SalesOrderItem();
            item.setProductId(product.getId());
            item.setName(product.getName());
            item.setPrice(product.getPrice()); // server-side price: the browser can't set its own
            item.setQty(e.getValue());
            order.addItem(item);
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(e.getValue())));
        }
        order.setSubtotal(subtotal);
        order.setTotal(subtotal);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto setPaymentMethod(String id, String method) {
        SalesOrder order = find(id);
        requireValidMethod(method);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only a pending order can change its payment method.");
        }
        order.setPaymentMethod(method);
        return toDto(orderRepository.save(order));
    }

    /** Marks an order as paid. Calling it again on an already-paid order is a harmless no-op. */
    @Transactional
    public OrderDto markPaid(String id, String method) {
        SalesOrder order = find(id);
        if (order.getStatus() == OrderStatus.PAID) {
            return toDto(order);
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("A cancelled order can't be paid.");
        }
        if (method != null && !method.isBlank()) {
            requireValidMethod(method);
            order.setPaymentMethod(method);
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(Instant.now());
        deductStock(order);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto cancel(String id) {
        SalesOrder order = find(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toDto(order);
        }
        if (order.getStatus() == OrderStatus.PAID) {
            throw new ConflictException("A paid order can't be cancelled.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toDto(orderRepository.save(order));
    }

    /**
     * Takes the sold quantities off each product's stock, in the same transaction as the payment.
     * Stock is clamped at zero: the customer has already paid, so a stock shortfall
     * (e.g. stock was edited meanwhile) must never block the payment. Deleted products are skipped.
     */
    private void deductStock(SalesOrder order) {
        for (SalesOrderItem item : order.getItems()) {
            productRepository.findByIdForUpdate(item.getProductId()).ifPresent(p ->
                    p.setStock(Math.max(0, p.getStock() - item.getQty())));
        }
    }

    private long nextOrderNumber() {
        Counter counter = counterRepository.lockByName(Counter.ORDER_NUMBER)
                .orElseGet(() -> counterRepository.save(new Counter(Counter.ORDER_NUMBER, 0L)));
        counter.setValue(counter.getValue() + 1);
        counterRepository.save(counter);
        return counter.getValue();
    }

    private void requireValidMethod(String method) {
        if (!PAYMENT_METHODS.contains(method)) {
            throw new IllegalArgumentException("Unknown payment method: " + method);
        }
    }

    private SalesOrder find(String id) {
        return orderRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Order", id));
    }

    private OrderDto toDto(SalesOrder o) {
        List<OrderItemDto> items = o.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getName(), i.getPrice(), i.getQty()))
                .toList();
        return new OrderDto(o.getId(), o.getNumber(), o.getInvoiceCode(), items, o.getCustomer().getId(),
                o.getPaymentMethod(), o.getSubtotal(), o.getTotal(), o.getStatus().name().toLowerCase(),
                o.getCreatedAt(), o.getPaidAt());
    }
}
