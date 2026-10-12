package com.ecommerce.order.dto;

import java.util.List;
import java.util.UUID;

public record ProductReservationResponse(
        UUID orderId, boolean reserved, String message, List<ProductReservationItemResponse> items
) {
}
