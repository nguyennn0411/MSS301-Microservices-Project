package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.CreateUserRequest;
import com.ecommerce.user.application.dto.UserResponse;
import com.ecommerce.user.domain.model.aggregate.Customer;
import com.ecommerce.user.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createsCustomerWithNormalizedEmail() {
        when(customerRepository.existsByEmailIgnoreCase("long@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("bcrypt-hash");
        when(customerRepository.saveAndFlush(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = customerService.create(
                new CreateUserRequest(" Long@Example.COM ", "Nguyen Bao Long", "0901234567", "Password123!", "CUSTOMER"));

        assertThat(response.email()).isEqualTo("long@example.com");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.role()).isEqualTo("CUSTOMER");
    }

    @Test
    void rejectsDuplicateEmail() {
        when(customerRepository.existsByEmailIgnoreCase("long@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(
                new CreateUserRequest("long@example.com", "Nguyen Bao Long", "0901234567", "Password123!", "CUSTOMER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already registered");
    }
}
