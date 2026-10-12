package com.ecommerce.order.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        Long orderCode,
        UUID userId,
        BigDecimal amount,
        String currency,
        String provider,
        String status,
        String paymentLinkId,
        String checkoutUrl,
        String qrCode,
        String providerReference,
        String statusReason,
        Instant createdAt,
        Instant updatedAt
) {
}
