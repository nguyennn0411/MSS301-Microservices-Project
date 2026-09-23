package com.ecommerce.order.application;

import com.ecommerce.order.client.ProductGatewayClient;
import com.ecommerce.order.client.UserGatewayClient;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.ProductReservationItemRequest;
import com.ecommerce.order.dto.ProductReservationItemResponse;
import com.ecommerce.order.dto.ProductReservationRequest;
import com.ecommerce.order.dto.ProductReservationResponse;
import com.ecommerce.order.dto.UserValidationResponse;
import com.ecommerce.order.exception.OrderIntegrationException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserGatewayClient userGatewayClient;
    private final ProductGatewayClient productGatewayClient;

    public OrderService(OrderRepository orderRepository, UserGatewayClient userGatewayClient,
                        ProductGatewayClient productGatewayClient) {
        this.orderRepository = orderRepository;
        this.userGatewayClient = userGatewayClient;
        this.productGatewayClient = productGatewayClient;
    }

    public OrderResponse create(CreateOrderRequest request) {
        UserValidationResponse user = userGatewayClient.validate(request.userId());
        if (user == null || !user.valid()) {
            throw new IllegalArgumentException(user == null ? "User validation returned no response" : user.message());
        }

        UUID orderId = UUID.randomUUID();
        ProductReservationRequest reservationRequest = new ProductReservationRequest(orderId,
                request.items().stream().map(item -> new ProductReservationItemRequest(
                        item.productId(), item.unitPrice(), item.quantity(), item.variantId(), item.size(), item.color()))
                        .toList());
        ProductReservationResponse reservation = productGatewayClient.reserve(reservationRequest);
        if (reservation == null || !reservation.reserved()) {
            throw new OrderIntegrationException("Inventory was not reserved", null);
        }

        try {
            Order order = new Order(orderId, request.userId(), request.buyerName().trim(),
                    request.buyerEmail().trim().toLowerCase(), trimToNull(request.description()),
                    request.currency().trim().toUpperCase(), request.shippingFee());
            for (ProductReservationItemResponse item : reservation.items()) {
                order.addItem(new OrderItem(item.productId(), item.variantId(), item.productName(), item.size(),
                        item.color(), item.quantity(), item.unitPrice()));
            }
            return toResponse(orderRepository.saveAndFlush(order));
        } catch (RuntimeException exception) {
            productGatewayClient.release(orderId);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(UUID userId) {
        List<Order> orders = userId == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream().map(this::toResponse).toList();
    }

    public OrderResponse cancel(UUID id, String reason) {
        Order order = load(id);
        order.cancel(reason.trim());
        productGatewayClient.release(id);
        return toResponse(orderRepository.saveAndFlush(order));
    }

    public OrderResponse confirm(UUID id) {
        Order order = load(id);
        order.confirm();
        return toResponse(orderRepository.saveAndFlush(order));
    }

    private Order load(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order %s was not found".formatted(id)));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream().map(item -> new OrderItemResponse(
                item.getId(), item.getProductId(), item.getVariantId(), item.getProductName(), item.getSize(),
                item.getColor(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal())).toList();
        return new OrderResponse(order.getId(), order.getUserId(), order.getBuyerName(), order.getBuyerEmail(),
                order.getDescription(), order.getCurrency(), order.getTotalAmount(), order.getShippingFee(),
                order.getStatus().name(), order.getFailureReason(), order.getPaidAt(), order.getCancelledAt(),
                items, order.getCreatedAt(), order.getUpdatedAt());
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
