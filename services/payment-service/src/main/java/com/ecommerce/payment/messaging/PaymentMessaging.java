package com.ecommerce.payment.messaging;

public final class PaymentMessaging {

    public static final String EXCHANGE = "payment.events";

    public static final String PAYMENT_CREATED = "payment.created";
    public static final String PAYMENT_SUCCESS = "payment.success";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_CANCELLED = "payment.cancelled";
    public static final String PAYMENT_REFUNDED = "payment.refunded";

    private PaymentMessaging() {
    }
}
