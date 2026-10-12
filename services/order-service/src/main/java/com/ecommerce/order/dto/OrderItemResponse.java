package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal, UUID variantId, String size, String color
) {
}
