package com.ecommerce.productcatalog.api.dto;

import java.util.UUID;

public record ProductVariantResponse(UUID id, String size, String color, String sku, int stockQuantity) {
}
