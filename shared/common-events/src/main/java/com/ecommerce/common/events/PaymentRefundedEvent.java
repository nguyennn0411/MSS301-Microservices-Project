package com.ecommerce.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRefundedEvent(
        String eventType,
        UUID orderId,
        Long orderCode,
        UUID paymentId,
        UUID userId,
        String buyerName,
        String buyerEmail,
        BigDecimal amount,
        String currency,
        String paymentProvider,
        String paymentLinkId,
        String providerReference,
        Instant occurredAt
) {
}
