package com.ecommerce.user.domain.model.aggregate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_challenges", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "purpose"}))
public class EmailChallenge {
    public enum Purpose { REGISTER, RESET }

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;
    @Column(nullable = false, length = 255)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Purpose purpose;
    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "full_name", length = 150)
    private String fullName;
    @Column(length = 20)
    private String phone;
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    protected EmailChallenge() {}

    public EmailChallenge(String email, Purpose purpose) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.purpose = purpose;
    }

    public void renew(String codeHash, LocalDateTime now) {
        this.codeHash = codeHash;
        this.sentAt = now;
        this.expiresAt = now.plusMinutes(10);
        this.attempts = 0;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public Purpose getPurpose() { return purpose; }
    public String getCodeHash() { return codeHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public int getAttempts() { return attempts; }
    public void incrementAttempts() { attempts++; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
