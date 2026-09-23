package com.ecommerce.productcatalog.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record InventoryReservationRequest(
        @NotNull UUID orderId,
        @NotEmpty List<@Valid ValidateProductItemRequest> items
) {
}
