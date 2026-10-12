package com.ecommerce.productcatalog.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ValidateProductsRequest(@NotEmpty List<@Valid ValidateProductItemRequest> items) {
}
