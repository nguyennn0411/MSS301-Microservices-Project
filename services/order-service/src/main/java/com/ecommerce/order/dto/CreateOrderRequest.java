package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID userId,
        @NotBlank @Size(max = 255) String buyerName,
        @NotBlank @Email @Size(max = 255) String buyerEmail,
        @Size(max = 255) String description,
        @NotBlank @Size(max = 10) String currency,
        @NotNull @DecimalMin("0.00") BigDecimal shippingFee,
        @NotEmpty List<@Valid CreateOrderItemRequest> items
) {
}
