package com.sathya.restaurant_management.controller;

import com.sathya.restaurant_management.dto.OrderDto;
import com.sathya.restaurant_management.dto.OrderRequest;
import com.sathya.restaurant_management.dto.PayRequest;
import com.sathya.restaurant_management.dto.PaymentMethodRequest;
import com.sathya.restaurant_management.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** All orders, newest first. */
    @GetMapping
    public List<OrderDto> getAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public OrderDto getById(@PathVariable String id) {
        return orderService.getById(id);
    }

    /** Body: { "customerId": "c_walkin", "items": [ { "productId": "p_salmon", "qty": 2 } ] } */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    /** Body: { "paymentMethod": "bakong" } - only while the order is still pending. */
    @PatchMapping("/{id}/payment-method")
    public OrderDto setPaymentMethod(@PathVariable String id, @Valid @RequestBody PaymentMethodRequest request) {
        return orderService.setPaymentMethod(id, request.paymentMethod());
    }

    /** Optional body: { "paymentMethod": "card" } */
    @PostMapping("/{id}/pay")
    public OrderDto pay(@PathVariable String id, @RequestBody(required = false) PayRequest request) {
        return orderService.markPaid(id, request == null ? null : request.paymentMethod());
    }

    @PostMapping("/{id}/cancel")
    public OrderDto cancel(@PathVariable String id) {
        return orderService.cancel(id);
    }
}
