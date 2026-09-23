package com.ecommerce.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 150) String fullName,
        @Pattern(regexp = "^$|^[0-9+() .-]{8,20}$", message = "phone has an invalid format") String phone,
        @NotBlank @Size(min = 8, max = 72) String password,
        String role
) {
}
