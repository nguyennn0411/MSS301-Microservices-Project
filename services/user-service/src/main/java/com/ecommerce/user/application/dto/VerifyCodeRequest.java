package com.ecommerce.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(@NotBlank @Email String email,
                                @NotBlank @Pattern(regexp = "[0-9]{6}") String code) {}
