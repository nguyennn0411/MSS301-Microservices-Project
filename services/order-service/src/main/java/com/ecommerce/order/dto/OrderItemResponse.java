package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id, UUID productId, UUID variantId, String productName, String size, String color,
        int quantity, BigDecimal unitPrice, BigDecimal lineTotal
) {
}
