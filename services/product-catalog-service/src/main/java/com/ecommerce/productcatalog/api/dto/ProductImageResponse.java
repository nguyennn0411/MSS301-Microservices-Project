package com.ecommerce.productcatalog.api.dto;

import java.util.UUID;

public record ProductImageResponse(UUID id, String imageUrl, boolean main) {
}
