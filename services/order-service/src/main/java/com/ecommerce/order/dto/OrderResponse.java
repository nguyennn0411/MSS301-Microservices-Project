package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id, UUID userId, String buyerName, String buyerEmail, String description, String currency,
        BigDecimal totalAmount, BigDecimal shippingFee, String status, String failureReason,
        LocalDateTime paidAt, LocalDateTime cancelledAt, List<OrderItemResponse> items,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {
}
