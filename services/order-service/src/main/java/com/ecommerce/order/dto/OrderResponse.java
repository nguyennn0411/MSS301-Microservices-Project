package com.ecommerce.order.dto;

import com.ecommerce.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        Long orderCode,
        UUID userId,
        String buyerName,
        String buyerEmail,
        BigDecimal totalAmount,
        String currency,
        OrderStatus status,
        UUID paymentId,
        String paymentLinkId,
        String checkoutUrl,
        String statusReason,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt, String buyerPhone, String shippingAddress, String customerNote, String paymentMethod, BigDecimal shippingFee
) {
}
