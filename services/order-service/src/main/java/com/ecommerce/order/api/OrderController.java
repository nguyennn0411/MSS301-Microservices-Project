package com.ecommerce.order.api;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.application.OrderService;
import com.ecommerce.order.dto.CancelOrderRequest;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request,
                                                              Authentication authentication) {
        requireOwnerOrAdmin(request.userId(), authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.create(request), "Order created and inventory reserved"));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> list(@RequestParam(required = false) UUID userId,
                                                  Authentication authentication) {
        UUID effectiveUserId = isAdmin(authentication) ? userId : UUID.fromString(authentication.getName());
        return ApiResponse.ok(orderService.list(effectiveUserId));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable UUID id, Authentication authentication) {
        OrderResponse order = orderService.get(id);
        requireOwnerOrAdmin(order.userId(), authentication);
        return ApiResponse.ok(order);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<OrderResponse> cancel(@PathVariable UUID id,
                                              @Valid @RequestBody CancelOrderRequest request,
                                              Authentication authentication) {
        OrderResponse order = orderService.get(id);
        requireOwnerOrAdmin(order.userId(), authentication);
        return ApiResponse.ok(orderService.cancel(id, request.reason()), "Order cancelled and inventory released");
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> confirm(@PathVariable UUID id) {
        return ApiResponse.ok(orderService.confirm(id), "Order confirmed");
    }

    private void requireOwnerOrAdmin(UUID userId, Authentication authentication) {
        if (!isAdmin(authentication) && !userId.toString().equals(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access another user's order");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
