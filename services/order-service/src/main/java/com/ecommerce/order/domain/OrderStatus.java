package com.ecommerce.order.domain;

public enum OrderStatus {
    PAYMENT_PENDING,
    PENDING_PAYMENT,
    CONFIRMED,
    PAID,
    PAYMENT_FAILED,
    CANCELLED,
    REFUNDED
}
