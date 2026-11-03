package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.RegisterRequest;
import com.ecommerce.user.application.dto.EmailRequest;
import com.ecommerce.user.application.dto.ResetPasswordRequest;
import com.ecommerce.user.application.dto.ChangePasswordRequest;
import com.ecommerce.user.application.dto.VerifyCodeRequest;
import com.ecommerce.user.domain.model.aggregate.Customer;
import com.ecommerce.user.domain.model.aggregate.EmailChallenge;
import com.ecommerce.user.domain.repository.CustomerRepository;
import com.ecommerce.user.domain.repository.EmailChallengeRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock CustomerRepository customerRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock EmailChallengeRepository challengeRepository;
    @Mock JavaMailSender mailSender;
    @InjectMocks AuthService authService;

    @Test
    void registersOnlyAfterEmailCodeIsVerified() {
        when(customerRepository.existsByEmailIgnoreCase("shopper@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("bcrypt-hash");
        when(passwordEncoder.encode(org.mockito.ArgumentMatchers.matches("[0-9]{6}"))).thenReturn("code-hash");
        when(passwordEncoder.matches(org.mockito.ArgumentMatchers.matches("[0-9]{6}"), org.mockito.ArgumentMatchers.eq("code-hash"))).thenReturn(true);
        when(customerRepository.saveAndFlush(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.issue(any(Customer.class))).thenReturn("signed.jwt");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        authService.register(new RegisterRequest(
                " Shopper@Example.com ", "Shopper", "0901234567", "Password123!"));
        org.mockito.Mockito.verify(customerRepository, org.mockito.Mockito.never()).saveAndFlush(any(Customer.class));
        ArgumentCaptor<EmailChallenge> challengeCaptor = ArgumentCaptor.forClass(EmailChallenge.class);
        verify(challengeRepository).saveAndFlush(challengeCaptor.capture());
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        String code = mailCaptor.getValue().getText().replaceAll("(?s).*?([0-9]{6}).*", "$1");
        when(challengeRepository.findByEmailAndPurpose("shopper@example.com", EmailChallenge.Purpose.REGISTER))
                .thenReturn(Optional.of(challengeCaptor.getValue()));

        var response = authService.verifyRegistration(new VerifyCodeRequest("shopper@example.com", code));

        assertThat(response.accessToken()).isEqualTo("signed.jwt");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("shopper@example.com");
        assertThat(response.user().role()).isEqualTo("CUSTOMER");
        verify(challengeRepository).delete(challengeCaptor.getValue());
    }

    @Test
    void demoPasswordHashMatchesDocumentedPassword() {
        assertThat(new BCryptPasswordEncoder().matches("password",
                "$2a$10$oM9l00sdBdaH5IkVpCCv..S5wksu9X6Qyx5rAaxjMckuSXCVXoDrC")).isTrue();
    }

    @Test
    void resetPasswordRequiresEmailedCodeAndConsumesIt() {
        Customer customer = new Customer("shopper@example.com", "Shopper", null, "old-hash", Customer.Role.CUSTOMER);
        EmailChallenge challenge = new EmailChallenge("shopper@example.com", EmailChallenge.Purpose.RESET);
        challenge.renew("code-hash", java.time.LocalDateTime.now());
        when(customerRepository.findByEmailIgnoreCase("shopper@example.com")).thenReturn(Optional.of(customer));
        when(challengeRepository.findByEmailAndPurpose("shopper@example.com", EmailChallenge.Purpose.RESET))
                .thenReturn(Optional.of(challenge));
        when(passwordEncoder.matches("123456", "code-hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("new-hash");

        authService.resetPassword(new ResetPasswordRequest("shopper@example.com", "123456", "NewPassword123!"));

        assertThat(customer.getPasswordHash()).isEqualTo("new-hash");
        verify(challengeRepository).delete(challenge);
    }

    @Test
    void incorrectCodeCountsTowardAttemptLimit() {
        EmailChallenge challenge = new EmailChallenge("shopper@example.com", EmailChallenge.Purpose.RESET);
        challenge.renew("code-hash", java.time.LocalDateTime.now());
        when(challengeRepository.findByEmailAndPurpose("shopper@example.com", EmailChallenge.Purpose.RESET))
                .thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("shopper@example.com", "000000", "NewPassword123!")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(challenge.getAttempts()).isEqualTo(1);
        verify(challengeRepository).save(challenge);
    }

    @Test
    void changePasswordRequiresCurrentPassword() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer("shopper@example.com", "Shopper", null, "old-hash", Customer.Role.CUSTOMER);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> authService.changePassword(id,
                new ChangePasswordRequest("wrong", "NewPassword123!")))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Current password is incorrect");
        assertThat(customer.getPasswordHash()).isEqualTo("old-hash");
    }
}
