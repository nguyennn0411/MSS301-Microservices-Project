package com.ecommerce.payment.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_order_id", columnNames = "order_id"),
                @UniqueConstraint(name = "uk_payments_order_code", columnNames = "order_code")
        }
)
public class PaymentEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "order_id", nullable = false, length = 36)
    private UUID orderId;

    @Column(name = "order_code", nullable = false)
    private Long orderCode;

    @Column(name = "user_id", nullable = false, length = 36)
    private UUID userId;

    @Column(name = "buyer_name", nullable = false, length = 150)
    private String buyerName;

    @Column(name = "buyer_email", nullable = false, length = 200)
    private String buyerEmail;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "payment_link_id", length = 100)
    private String paymentLinkId;

    @Column(name = "checkout_url", length = 1000)
    private String checkoutUrl;

    @Lob
    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "provider_reference", length = 150)
    private String providerReference;

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PaymentEntity() {
    }

    public static PaymentEntity create(
            UUID orderId,
            Long orderCode,
            UUID userId,
            String buyerName,
            String buyerEmail,
            BigDecimal amount,
            String currency
    ) {
        PaymentEntity payment = new PaymentEntity();
        payment.id = UUID.randomUUID();
        payment.orderId = orderId;
        payment.orderCode = orderCode;
        payment.userId = userId;
        payment.buyerName = buyerName;
        payment.buyerEmail = buyerEmail;
        payment.amount = amount;
        payment.currency = currency;
        payment.provider = "PAYOS";
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = Instant.now();
        payment.updatedAt = payment.createdAt;
        return payment;
    }

    public void useCod() { this.provider="COD"; }
    public void attachPaymentLink(
            String paymentLinkId,
            String checkoutUrl,
            String qrCode
    ) {
        this.paymentLinkId = paymentLinkId;
        this.checkoutUrl = checkoutUrl;
        this.qrCode = qrCode;
        touch();
    }

    public void markPaid(String providerReference) {
        if (this.status == PaymentStatus.PAID) {
            return;
        }
        if (this.status == PaymentStatus.CANCELLED || this.status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("A cancelled or refunded payment cannot become paid.");
        }
        this.status = PaymentStatus.PAID;
        this.providerReference = providerReference;
        this.statusReason = null;
        touch();
    }

    public void markFailed(String reason) {
        if (this.status == PaymentStatus.PAID || this.status == PaymentStatus.CANCELLED
                || this.status == PaymentStatus.REFUNDED) {
            return;
        }
        this.status = PaymentStatus.FAILED;
        this.statusReason = reason;
        touch();
    }

    public void markCancelled(String reason) {
        if (this.status == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Paid payment cannot be cancelled.");
        }
        this.status = PaymentStatus.CANCELLED;
        this.statusReason = reason;
        touch();
    }

    public void markRefunded(String providerReference) {
        if (this.status != PaymentStatus.PAID) {
            throw new IllegalStateException("Only a paid payment can be refunded.");
        }
        this.status = PaymentStatus.REFUNDED;
        this.providerReference = providerReference;
        this.statusReason = null;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (currency == null || currency.isBlank()) currency = "VND";
        if (provider == null || provider.isBlank()) provider = "PAYOS";
        if (status == null) status = PaymentStatus.PENDING;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getProvider() {
        return provider;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
