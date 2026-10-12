package com.ecommerce.productcatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 20)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(length = 100)
    private String sku;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProductVariant() {
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) throw new IllegalArgumentException("stockQuantity must not be negative");
        this.stockQuantity = stockQuantity;
    }
    public void reserve(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("quantity must be at least 1");
        if (stockQuantity < quantity) throw new InsufficientStockException("Insufficient stock for SKU " + sku);
        stockQuantity -= quantity;
    }
    public void release(int quantity) { stockQuantity += quantity; }
}
