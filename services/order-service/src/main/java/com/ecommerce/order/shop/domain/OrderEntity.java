package com.ecommerce.order.shop.domain;

import com.ecommerce.order.domain.OrderStatus;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_order_code", columnNames = "order_code")
        }
)
public class OrderEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "order_code", nullable = false)
    private Long orderCode;

    @Column(name = "user_id", nullable = false, length = 36)
    private UUID userId;

    @Column(name = "buyer_name", nullable = false, length = 150)
    private String buyerName;

    @Column(name = "buyer_email", nullable = false, length = 200)
    private String buyerEmail;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "payment_id", length = 36)
    private UUID paymentId;

    @Column(name = "payment_link_id", length = 100)
    private String paymentLinkId;

    @Column(name = "checkout_url", length = 1000)
    private String checkoutUrl;

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OrderBy("createdAt ASC")
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(name="buyer_phone", length=25)
    private String buyerPhone;
    @Column(name="shipping_address", length=500)
    private String shippingAddress;
    @Column(name="customer_note", length=500)
    private String customerNote;
    @Column(name="payment_method", length=20, nullable=false)
    private String paymentMethod = "PAYOS";
    @Column(name="shipping_fee", precision=19, scale=2, nullable=false)
    private BigDecimal shippingFee = BigDecimal.ZERO;
    @Column(name="request_key", length=36)
    private UUID requestKey;
    public void setDelivery(String phone, String address, String note, String method, BigDecimal fee, UUID key) {
        buyerPhone=phone; shippingAddress=address; customerNote=note; paymentMethod=method; shippingFee=fee; requestKey=key;
        recalculateTotal();
    }
    public void confirmCod() { status=OrderStatus.CONFIRMED; touch(); }
    public String getBuyerPhone(){ return buyerPhone; }
    public String getShippingAddress(){ return shippingAddress; }
    public String getCustomerNote(){ return customerNote; }
    public String getPaymentMethod(){ return paymentMethod; }
    public BigDecimal getShippingFee(){ return shippingFee; }
    public OrderEntity() {
    }

    public static OrderEntity create(
            Long orderCode,
            UUID userId,
            String buyerName,
            String buyerEmail,
            String currency
    ) {
        OrderEntity order = new OrderEntity();
        order.id = UUID.randomUUID();
        order.orderCode = orderCode;
        order.userId = userId;
        order.buyerName = buyerName;
        order.buyerEmail = buyerEmail;
        order.currency = currency;
        order.totalAmount = BigDecimal.ZERO;
        order.status = OrderStatus.PENDING_PAYMENT;
        order.createdAt = Instant.now();
        order.updatedAt = order.createdAt;
        return order;
    }

    public void addItem(OrderItemEntity item) {
        item.setOrder(this);
        this.items.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItemEntity::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).add(shippingFee);
        touch();
    }

    public void attachPayment(UUID paymentId, String paymentLinkId, String checkoutUrl) {
        this.paymentId = paymentId;
        this.paymentLinkId = paymentLinkId;
        this.checkoutUrl = checkoutUrl;
        touch();
    }

    public void markPaid(UUID paymentId, String paymentLinkId) {
        if (this.status == OrderStatus.PAID) {
            return;
        }
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.REFUNDED) {
            return;
        }
        this.paymentId = paymentId != null ? paymentId : this.paymentId;
        this.paymentLinkId = paymentLinkId != null ? paymentLinkId : this.paymentLinkId;
        this.status = OrderStatus.PAID;
        this.statusReason = null;
        touch();
    }

    public void markPaymentFailed(String reason) {
        if (this.status == OrderStatus.PAID || this.status == OrderStatus.CANCELLED
                || this.status == OrderStatus.REFUNDED) {
            return;
        }
        this.status = OrderStatus.PAYMENT_FAILED;
        this.statusReason = reason;
        touch();
    }

    public void markCancelled(String reason) {
        if (this.status == OrderStatus.PAID) {
            throw new IllegalArgumentException("Paid order cannot be cancelled.");
        }
        this.status = OrderStatus.CANCELLED;
        this.statusReason = reason;
        touch();
    }

    public void markRefunded() {
        if (this.status != OrderStatus.PAID) {
            return;
        }
        this.status = OrderStatus.REFUNDED;
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
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (status == null) status = OrderStatus.PENDING_PAYMENT;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
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

    public List<OrderItemEntity> getItems() {
        return items;
    }
}
