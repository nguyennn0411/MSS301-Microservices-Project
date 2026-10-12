package com.ecommerce.productcatalog.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ValidateProductItemRequest(
        @NotNull UUID productId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice,
        @NotNull @Min(1) Integer quantity,
        UUID variantId,
        String size,
        String color
) {
}
