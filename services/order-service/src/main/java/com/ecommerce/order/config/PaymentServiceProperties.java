package com.ecommerce.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment-service")
public record PaymentServiceProperties(
        String baseUrl,
        String createPayosPath,
        String cancelPayosPath
) {
}
