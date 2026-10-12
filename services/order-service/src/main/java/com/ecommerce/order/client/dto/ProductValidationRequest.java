package com.ecommerce.order.client.dto;

import java.util.List;
import java.util.UUID;

public record ProductValidationRequest(
        List<Item> items
) {
    public record Item(
            UUID productId,
            int quantity, UUID variantId
    ) {
    }
}
