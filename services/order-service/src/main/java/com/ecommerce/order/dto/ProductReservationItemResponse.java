package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductReservationItemResponse(
        UUID productId, String productName, UUID variantId, String size, String color,
        BigDecimal unitPrice, int quantity
) {
}
