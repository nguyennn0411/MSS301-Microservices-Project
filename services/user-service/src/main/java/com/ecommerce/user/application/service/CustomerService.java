package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.CreateUserRequest;
import com.ecommerce.user.application.dto.UpdateUserRequest;
import com.ecommerce.user.application.dto.UserResponse;
import com.ecommerce.user.domain.model.aggregate.Customer;
import com.ecommerce.user.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        return toResponse(customerRepository.saveAndFlush(
                new Customer(email, request.fullName().trim(), trimToNull(request.phone()),
                        passwordEncoder.encode(request.password()), parseRole(request.role(), Customer.Role.CUSTOMER))));
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse update(UUID id, UpdateUserRequest request) {
        Customer customer = load(id);
        String email = normalizeEmail(request.email());
        customerRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new IllegalArgumentException("Email is already registered"); });

        customer.setEmail(email);
        customer.setFullName(request.fullName().trim());
        customer.setPhone(trimToNull(request.phone()));
        customer.setStatus(parseStatus(request.status()));
        return toResponse(customerRepository.saveAndFlush(customer));
    }

    public UserResponse deactivate(UUID id) {
        Customer customer = load(id);
        customer.setStatus(Customer.Status.INACTIVE);
        return toResponse(customerRepository.saveAndFlush(customer));
    }

    private Customer load(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User %s was not found".formatted(id)));
    }

    private Customer.Status parseStatus(String value) {
        try {
            return Customer.Status.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("status must be ACTIVE or INACTIVE");
        }
    }

    private Customer.Role parseRole(String value, Customer.Role fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Customer.Role.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("role must be CUSTOMER or ADMIN");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private UserResponse toResponse(Customer customer) {
        return new UserResponse(customer.getId(), customer.getEmail(), customer.getFullName(), customer.getPhone(),
                customer.getStatus().name(), customer.getRole().name(), customer.getCreatedAt(), customer.getUpdatedAt());
    }
}
