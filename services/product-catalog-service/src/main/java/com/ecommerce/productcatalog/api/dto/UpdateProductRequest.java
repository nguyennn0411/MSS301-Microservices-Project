package com.ecommerce.productcatalog.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 100) String brand,
        @Size(max = 5000) String description,
        @NotNull UUID categoryId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal basePrice,
        @NotBlank String status,
        List<@Valid ProductVariantRequest> variants,
        List<@Valid ProductImageRequest> images
) {
}
