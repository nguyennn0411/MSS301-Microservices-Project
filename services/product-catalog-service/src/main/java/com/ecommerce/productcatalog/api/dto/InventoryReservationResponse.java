package com.ecommerce.productcatalog.api.dto;

import java.util.List;
import java.util.UUID;

public record InventoryReservationResponse(
        UUID orderId,
        boolean reserved,
        String message,
        List<InventoryReservationItemResponse> items
) {
}
