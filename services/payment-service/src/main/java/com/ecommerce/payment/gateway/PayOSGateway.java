package com.ecommerce.payment.gateway;

import com.ecommerce.payment.config.PayOSProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

@Component
public class PayOSGateway {

    private final ObjectProvider<PayOS> payOSProvider;
    private final PayOSProperties properties;

    public PayOSGateway(
            ObjectProvider<PayOS> payOSProvider,
            PayOSProperties properties
    ) {
        this.payOSProvider = payOSProvider;
        this.properties = properties;
    }

    public CreatePaymentLinkResponse createPaymentLink(
            Long orderCode,
            long amount
    ) {
        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(buildDescription(orderCode))
                .returnUrl(properties.returnUrl())
                .cancelUrl(properties.cancelUrl())
                .build();

        return client().paymentRequests().create(request);
    }

    public void cancelPaymentLink(Long orderCode, String reason) {
        client().paymentRequests().cancel(orderCode, reason);
    }

    public PaymentLink getPaymentLink(Long orderCode) {
        return client().paymentRequests().get(orderCode);
    }

    public WebhookData verifyWebhook(Webhook webhook) {
        return client().webhooks().verify(webhook);
    }

    public Object confirmWebhook() {
        if (properties.webhookUrl() == null || properties.webhookUrl().isBlank()) {
            throw new IllegalArgumentException("PAYOS_WEBHOOK_URL is required.");
        }
        return client().webhooks().confirm(properties.webhookUrl());
    }

    private PayOS client() {
        PayOS client = payOSProvider.getIfAvailable();

        if (client == null) {
            throw new IllegalStateException(
                    "PayOS is disabled. Set PAYOS_ENABLED=true and configure PayOS secrets."
            );
        }

        return client;
    }

    private String buildDescription(Long orderCode) {
        return "Order " + orderCode;
    }
}
