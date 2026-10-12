package com.ecommerce.order.domain;

public enum OrderStatus {
    CONFIRMED, PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    CANCELLED,
    REFUNDED
}
