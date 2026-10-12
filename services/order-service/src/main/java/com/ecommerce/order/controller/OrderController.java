package com.ecommerce.order.controller;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ApiResponse.ok(
                orderService.create(request),
                "Order created. Complete payment using checkoutUrl."
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getById(@PathVariable("id") UUID id) {
        return ApiResponse.ok(orderService.getById(id));
    }

    @GetMapping("/code/{orderCode}")
    public ApiResponse<OrderResponse> getByOrderCode(
            @PathVariable("orderCode") Long orderCode
    ) {
        return ApiResponse.ok(orderService.getByOrderCode(orderCode));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) CancelOrderRequest request
    ) {
        return ApiResponse.ok(
                orderService.cancel(
                        id,
                        request != null ? request.reason() : null
                ),
                "Order cancelled."
        );
    }

    public record CancelOrderRequest(String reason) {
    }
}
