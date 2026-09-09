package com.ecommerce.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCreatedEvent(
        String eventType,
        UUID orderId,
        Long orderCode,
        UUID paymentId,
        UUID userId,
        String buyerName,
        String buyerEmail,
        String paymentLinkId,
        String checkoutUrl,
        String qrCode,
        BigDecimal amount,
        String currency,
        String paymentProvider,
        Instant occurredAt
) {
}
