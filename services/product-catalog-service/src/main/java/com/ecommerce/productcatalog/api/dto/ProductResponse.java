package com.ecommerce.productcatalog.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String brand,
        String description,
        UUID categoryId,
        String categoryName,
        BigDecimal basePrice,
        String status,
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
