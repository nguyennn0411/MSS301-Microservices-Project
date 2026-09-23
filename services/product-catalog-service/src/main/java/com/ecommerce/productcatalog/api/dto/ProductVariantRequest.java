package com.ecommerce.productcatalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public record ProductVariantRequest(
        @NotBlank @Size(max = 20) String size,
        @Size(max = 50) String color,
        @Size(max = 100) String sku,
        @Min(0) Integer stockQuantity
) {
}
