package com.ecommerce.order.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        UUID orderId,
        Long orderCode,
        UUID userId,
        String buyerName,
        String buyerEmail,
        BigDecimal amount,
        String currency
) {
}
