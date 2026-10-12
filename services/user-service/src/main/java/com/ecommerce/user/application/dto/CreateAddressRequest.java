package com.ecommerce.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAddressRequest(
        @NotNull UUID userId,
        @NotBlank @Size(max = 150) String receiverName,
        @NotBlank @Pattern(regexp = "^[0-9+() .-]{8,20}$", message = "receiverPhone has an invalid format") String receiverPhone,
        @NotBlank @Size(max = 255) String addressLine,
        @Size(max = 100) String ward,
        @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 20) String postalCode,
        boolean defaultAddress
) {
}
