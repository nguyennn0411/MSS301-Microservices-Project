package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.AuthResponse;
import com.ecommerce.user.application.dto.ChangePasswordRequest;
import com.ecommerce.user.application.dto.EmailRequest;
import com.ecommerce.user.application.dto.LoginRequest;
import com.ecommerce.user.application.dto.RegisterRequest;
import com.ecommerce.user.application.dto.ResetPasswordRequest;
import com.ecommerce.user.application.dto.UserResponse;
import com.ecommerce.user.application.dto.VerifyCodeRequest;
import com.ecommerce.user.domain.model.aggregate.Customer;
import com.ecommerce.user.domain.model.aggregate.EmailChallenge;
import com.ecommerce.user.domain.repository.CustomerRepository;
import com.ecommerce.user.domain.repository.EmailChallengeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailChallengeRepository challengeRepository;
    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final SecureRandom random = new SecureRandom();

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       EmailChallengeRepository challengeRepository, JavaMailSender mailSender,
                       @Value("${spring.mail.username}") String mailFrom) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.challengeRepository = challengeRepository;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        EmailChallenge challenge = getOrCreate(email, EmailChallenge.Purpose.REGISTER);
        checkCooldown(challenge);
        challenge.setFullName(request.fullName().trim());
        challenge.setPhone(trimToNull(request.phone()));
        challenge.setPasswordHash(passwordEncoder.encode(request.password()));
        sendCode(challenge);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public AuthResponse verifyRegistration(VerifyCodeRequest request) {
        EmailChallenge challenge = validChallenge(request.email(), request.code(), EmailChallenge.Purpose.REGISTER);
        if (customerRepository.existsByEmailIgnoreCase(challenge.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        Customer customer = customerRepository.saveAndFlush(new Customer(challenge.getEmail(), challenge.getFullName(),
                challenge.getPhone(), challenge.getPasswordHash(), Customer.Role.CUSTOMER));
        challengeRepository.delete(challenge);
        return response(customer);
    }

    public void forgotPassword(EmailRequest request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.findByEmailIgnoreCase(email)
                .filter(customer -> customer.getStatus() == Customer.Status.ACTIVE).isEmpty()) return;
        EmailChallenge challenge = getOrCreate(email, EmailChallenge.Purpose.RESET);
        if (challenge.getSentAt() != null && LocalDateTime.now().isBefore(challenge.getSentAt().plusSeconds(60))) return;
        sendCode(challenge);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void resetPassword(ResetPasswordRequest request) {
        EmailChallenge challenge = validChallenge(request.email(), request.code(), EmailChallenge.Purpose.RESET);
        Customer customer = customerRepository.findByEmailIgnoreCase(challenge.getEmail())
                .filter(found -> found.getStatus() == Customer.Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code"));
        customer.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        challengeRepository.delete(challenge);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        Customer customer = customerRepository.findById(userId)
                .filter(found -> found.getStatus() == Customer.Status.ACTIVE)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.currentPassword(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        customer.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private EmailChallenge getOrCreate(String email, EmailChallenge.Purpose purpose) {
        return challengeRepository.findByEmailAndPurpose(email, purpose)
                .orElseGet(() -> new EmailChallenge(email, purpose));
    }

    private void checkCooldown(EmailChallenge challenge) {
        if (challenge.getSentAt() != null && LocalDateTime.now().isBefore(challenge.getSentAt().plusSeconds(60))) {
            throw new IllegalArgumentException("Please wait 60 seconds before requesting another code");
        }
    }

    private void sendCode(EmailChallenge challenge) {
        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        challenge.renew(passwordEncoder.encode(code), LocalDateTime.now());
        challengeRepository.saveAndFlush(challenge);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(challenge.getEmail());
        message.setSubject(challenge.getPurpose() == EmailChallenge.Purpose.REGISTER
                ? "MSS Studio - Xac nhan dang ky" : "MSS Studio - Dat lai mat khau");
        message.setText("Ma xac nhan cua ban: " + code + "\nMa co hieu luc trong 10 phut. Khong chia se ma nay.");
        mailSender.send(message);
    }

    private EmailChallenge validChallenge(String email, String code, EmailChallenge.Purpose purpose) {
        EmailChallenge challenge = challengeRepository.findByEmailAndPurpose(normalizeEmail(email), purpose)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code"));
        if (LocalDateTime.now().isAfter(challenge.getExpiresAt()) || challenge.getAttempts() >= 5) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }
        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            challenge.incrementAttempts();
            challengeRepository.save(challenge);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }
        return challenge;
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
