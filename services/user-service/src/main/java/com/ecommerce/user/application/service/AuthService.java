package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.AuthResponse;
import com.ecommerce.user.application.dto.LoginRequest;
import com.ecommerce.user.application.dto.RegisterRequest;
import com.ecommerce.user.application.dto.UserResponse;
import com.ecommerce.user.domain.model.aggregate.Customer;
import com.ecommerce.user.domain.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AuthService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        Customer customer = customerRepository.saveAndFlush(new Customer(
                email, request.fullName().trim(), trimToNull(request.phone()),
                passwordEncoder.encode(request.password()), Customer.Role.CUSTOMER));
        return response(customer);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (customer.getStatus() != Customer.Status.ACTIVE
                || !passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return response(customer);
    }

    private AuthResponse response(Customer customer) {
        UserResponse user = new UserResponse(customer.getId(), customer.getEmail(), customer.getFullName(),
                customer.getPhone(), customer.getStatus().name(), customer.getRole().name(),
                customer.getCreatedAt(), customer.getUpdatedAt());
        return new AuthResponse(jwtService.issue(customer), "Bearer", jwtService.expiresInSeconds(), user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
