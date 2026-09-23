package com.ecommerce.order.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "char(36)")
    private UUID userId;

    @Column(name = "buyer_name", nullable = false)
    private String buyerName;

    @Column(name = "buyer_email", nullable = false)
    private String buyerEmail;

    private String description;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Column(name = "payment_id", columnDefinition = "char(36)")
    private UUID paymentId;
    @Column(name = "payment_order_code")
    private Long paymentOrderCode;
    @Column(name = "payment_link_id")
    private String paymentLinkId;
    @Column(name = "checkout_url", columnDefinition = "text")
    private String checkoutUrl;
    @Column(name = "qr_code", columnDefinition = "text")
    private String qrCode;
    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Order() {
    }

    public Order(UUID id, UUID userId, String buyerName, String buyerEmail, String description,
                 String currency, BigDecimal shippingFee) {
        this.id = id;
        this.userId = userId;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.description = description;
        this.currency = currency;
        this.shippingFee = shippingFee;
        this.totalAmount = shippingFee;
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
        totalAmount = totalAmount.add(item.getLineTotal());
    }

    public void confirm() {
        if (status != OrderStatus.PAYMENT_PENDING) throw new IllegalStateException("Only pending orders can be confirmed");
        status = OrderStatus.CONFIRMED;
        paidAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (status == OrderStatus.CONFIRMED) throw new IllegalStateException("Confirmed orders cannot be cancelled");
        if (status == OrderStatus.CANCELLED) return;
        status = OrderStatus.CANCELLED;
        failureReason = reason;
        cancelledAt = LocalDateTime.now();
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getBuyerName() { return buyerName; }
    public String getBuyerEmail() { return buyerEmail; }
    public String getDescription() { return description; }
    public String getCurrency() { return currency; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public OrderStatus getStatus() { return status; }
    public UUID getPaymentId() { return paymentId; }
    public Long getPaymentOrderCode() { return paymentOrderCode; }
    public String getPaymentLinkId() { return paymentLinkId; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public String getQrCode() { return qrCode; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public List<OrderItem> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
