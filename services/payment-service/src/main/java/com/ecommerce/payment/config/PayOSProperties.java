package com.ecommerce.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payos")
public record PayOSProperties(
        boolean enabled,
        String clientId,
        String apiKey,
        String checksumKey,
        String returnUrl,
        String cancelUrl,
        String webhookUrl
) {
}
