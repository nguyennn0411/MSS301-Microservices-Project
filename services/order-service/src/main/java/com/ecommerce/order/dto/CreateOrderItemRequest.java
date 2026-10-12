package com.ecommerce.order.dto;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record CreateOrderItemRequest(@NotNull UUID productId, @Min(1) @Max(10) int quantity, UUID variantId) {}
