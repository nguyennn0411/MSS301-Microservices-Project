package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductReservationItemRequest(
        UUID productId, BigDecimal unitPrice, Integer quantity, UUID variantId, String size, String color
) {
}
