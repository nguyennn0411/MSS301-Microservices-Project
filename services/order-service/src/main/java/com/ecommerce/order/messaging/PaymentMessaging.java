package com.ecommerce.order.messaging;

public final class PaymentMessaging {

    public static final String EXCHANGE = "payment.events";
    public static final String ORDER_QUEUE = "order.payment.events";
    public static final String ALL_PAYMENT_EVENTS = "payment.*";

    public static final String PAYMENT_CREATED = "payment.created";
    public static final String PAYMENT_SUCCESS = "payment.success";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_CANCELLED = "payment.cancelled";
    public static final String PAYMENT_REFUNDED = "payment.refunded";

    private PaymentMessaging() {
    }
}
