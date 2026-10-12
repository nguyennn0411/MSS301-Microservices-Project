package com.ecommerce.order.dto;

import java.util.UUID;

public record UserValidationResponse(UUID userId, boolean valid, String message) {
}
