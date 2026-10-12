package com.ecommerce.productcatalog.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryReservationItemResponse(
        UUID productId,
        String productName,
        UUID variantId,
        String size,
        String color,
        BigDecimal unitPrice,
        int quantity
) {
}
