package com.ecommerce.order.shop.domain;

import com.ecommerce.order.domain.OrderStatus;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "product_id", nullable = false, length = 36)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name="variant_id",length=36)
    private UUID variantId;
    @Column(name="variant_size",length=20)
    private String size;
    @Column(name="variant_color",length=50)
    private String color;
    public void setVariant(UUID id,String size,String color){this.variantId=id;this.size=size;this.color=color;}
    public UUID getVariantId(){return variantId;}
    public String getSize(){return size;}
    public String getColor(){return color;}
    public OrderItemEntity() {
    }

    public static OrderItemEntity create(
            UUID productId,
            String productName,
            BigDecimal unitPrice,
            int quantity
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }

        OrderItemEntity item = new OrderItemEntity();
        item.id = UUID.randomUUID();
        item.productId = productId;
        item.productName = productName;
        item.unitPrice = unitPrice;
        item.quantity = quantity;
        item.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        item.createdAt = Instant.now();
        return item;
    }

    void setOrder(OrderEntity order) {
        this.order = order;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
