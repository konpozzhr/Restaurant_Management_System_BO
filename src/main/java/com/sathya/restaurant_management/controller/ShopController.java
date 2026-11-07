package com.sathya.restaurant_management.controller;

import com.sathya.restaurant_management.dto.CategoryDto;
import com.sathya.restaurant_management.dto.OrderDto;
import com.sathya.restaurant_management.dto.OrderRequest;
import com.sathya.restaurant_management.dto.PayRequest;
import com.sathya.restaurant_management.dto.PaymentMethodRequest;
import com.sathya.restaurant_management.dto.ShopMenuDto;
import com.sathya.restaurant_management.dto.ShopProductDto;
import com.sathya.restaurant_management.model.Customer;
import com.sathya.restaurant_management.service.CategoryService;
import com.sathya.restaurant_management.service.OrderService;
import com.sathya.restaurant_management.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoints for the customer storefront (/shop in the React app). No sign-in needed,
 * so they are deliberately small: read the menu, place an order, and settle THAT order.
 * Everything else (order list, menu editing, stock, customers, costs) stays behind the staff login.
 *
 * Note: /pay trusts the browser, exactly like the frontend's simulated payment flow did.
 * Before taking real money, call your payment provider here and only mark the order paid
 * once the provider confirms.
 */
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;

    @GetMapping("/menu")
    public ShopMenuDto menu() {
        var products = productService.findAll().stream()
                .map(p -> new ShopProductDto(p.id(), p.name(), p.category(), p.price(), p.stock(),
                        p.icon(), p.color(), p.photo()))
                .toList();
        var categories = categoryService.findAll().stream().map(CategoryDto::name).toList();
        return new ShopMenuDto(products, categories);
    }

    /** Online orders are always for the walk-in customer - a stranger can't attach an order to someone's profile. */
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.create(new OrderRequest(Customer.WALK_IN_ID, request.items()));
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable String id) {
        return orderService.getById(id);
    }

    @PatchMapping("/orders/{id}/payment-method")
    public OrderDto setPaymentMethod(@PathVariable String id, @Valid @RequestBody PaymentMethodRequest request) {
        return orderService.setPaymentMethod(id, request.paymentMethod());
    }

    @PostMapping("/orders/{id}/pay")
    public OrderDto pay(@PathVariable String id, @RequestBody(required = false) PayRequest request) {
        return orderService.markPaid(id, request == null ? null : request.paymentMethod());
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderDto cancel(@PathVariable String id) {
        return orderService.cancel(id);
    }
}
