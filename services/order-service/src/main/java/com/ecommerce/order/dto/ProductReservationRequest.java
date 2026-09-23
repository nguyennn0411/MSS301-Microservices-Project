package com.ecommerce.order.dto;

import java.util.List;
import java.util.UUID;

public record ProductReservationRequest(UUID orderId, List<ProductReservationItemRequest> items) {
}
