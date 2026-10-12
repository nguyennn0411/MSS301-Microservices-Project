package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.RegisterRequest;
import com.ecommerce.user.domain.model.aggregate.Customer;
import com.ecommerce.user.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock CustomerRepository customerRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    @Test
    void registersCustomerAndReturnsBearerToken() {
        when(customerRepository.existsByEmailIgnoreCase("shopper@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("bcrypt-hash");
        when(customerRepository.saveAndFlush(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.issue(any(Customer.class))).thenReturn("signed.jwt");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        var response = authService.register(new RegisterRequest(
                " Shopper@Example.com ", "Shopper", "0901234567", "Password123!"));

        assertThat(response.accessToken()).isEqualTo("signed.jwt");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("shopper@example.com");
        assertThat(response.user().role()).isEqualTo("CUSTOMER");
    }

    @Test
    void demoPasswordHashMatchesDocumentedPassword() {
        assertThat(new BCryptPasswordEncoder().matches("password",
                "$2a$10$oM9l00sdBdaH5IkVpCCv..S5wksu9X6Qyx5rAaxjMckuSXCVXoDrC")).isTrue();
    }
}
