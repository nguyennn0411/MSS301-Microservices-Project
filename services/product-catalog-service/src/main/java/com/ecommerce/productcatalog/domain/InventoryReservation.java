package com.ecommerce.productcatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservation {
    public enum Status { RESERVED, RELEASED }

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "char(36)")
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected InventoryReservation() {
    }

    public InventoryReservation(UUID orderId, ProductVariant variant, int quantity) {
        this.orderId = orderId;
        this.variant = variant;
        this.quantity = quantity;
        this.status = Status.RESERVED;
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
    public UUID getOrderId() { return orderId; }
    public ProductVariant getVariant() { return variant; }
    public int getQuantity() { return quantity; }
    public Status getStatus() { return status; }
    public void release() { status = Status.RELEASED; }
}
