package com.ecommerce.user.api;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.user.application.dto.AuthResponse;
import com.ecommerce.user.application.dto.ChangePasswordRequest;
import com.ecommerce.user.application.dto.EmailRequest;
import com.ecommerce.user.application.dto.LoginRequest;
import com.ecommerce.user.application.dto.RegisterRequest;
import com.ecommerce.user.application.dto.ResetPasswordRequest;
import com.ecommerce.user.application.dto.VerifyCodeRequest;
import com.ecommerce.user.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Verification code sent"));
    }

    @PostMapping("/verify-registration")
    public ApiResponse<AuthResponse> verifyRegistration(@Valid @RequestBody VerifyCodeRequest request) {
        return ApiResponse.ok(authService.verifyRegistration(request), "Registration successful");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody EmailRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(null, "If this account exists, a verification code has been sent");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(null, "Password reset successful");
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(UUID.fromString(jwt.getSubject()), request);
        return ApiResponse.ok(null, "Password changed successfully");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), "Login successful");
    }
}
