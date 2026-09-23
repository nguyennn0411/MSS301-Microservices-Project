package com.ecommerce.productcatalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductImageRequest(
        @NotBlank @Size(max = 2000) String imageUrl,
        Boolean main
) {
}
