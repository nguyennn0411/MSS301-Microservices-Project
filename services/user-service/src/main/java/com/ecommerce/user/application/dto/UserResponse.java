package com.ecommerce.user.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String status,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
