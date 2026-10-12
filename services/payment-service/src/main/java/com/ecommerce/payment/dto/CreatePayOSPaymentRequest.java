package com.ecommerce.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePayOSPaymentRequest(
        @NotNull UUID orderId,
        @NotNull Long orderCode,
        @NotNull UUID userId,
        @NotBlank String buyerName,
        @NotBlank @Email String buyerEmail,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency
) {
}
