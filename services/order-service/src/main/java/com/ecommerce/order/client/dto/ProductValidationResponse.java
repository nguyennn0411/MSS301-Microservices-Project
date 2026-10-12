package com.ecommerce.order.client.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductValidationResponse(
        boolean valid,
        List<Item> items,
        String message
) {
    public record Item(
            UUID productId,
            String productName,
            BigDecimal unitPrice,
            boolean available,
            String reason, UUID variantId, String size, String color
    ) {
    }
}
