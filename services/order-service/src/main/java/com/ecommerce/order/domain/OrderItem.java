package com.ecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", columnDefinition = "char(36)")
    private UUID productId;
    @Column(name = "variant_id", columnDefinition = "char(36)")
    private UUID variantId;
    @Column(name = "product_name", nullable = false)
    private String productName;
    @Column(nullable = false, length = 50)
    private String size;
    @Column(length = 100)
    private String color;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;
    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    protected OrderItem() {
    }

    public OrderItem(UUID productId, UUID variantId, String productName, String size, String color,
                     int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.size = size;
        this.color = color;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @PrePersist
    void onCreate() { if (id == null) id = UUID.randomUUID(); }

    void setOrder(Order order) { this.order = order; }
    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
