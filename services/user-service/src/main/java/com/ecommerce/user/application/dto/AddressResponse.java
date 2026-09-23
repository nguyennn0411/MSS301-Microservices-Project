package com.ecommerce.user.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponse(
        Long id,
        UUID userId,
        String receiverName,
        String receiverPhone,
        String addressLine,
        String ward,
        String district,
        String city,
        String postalCode,
        boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
