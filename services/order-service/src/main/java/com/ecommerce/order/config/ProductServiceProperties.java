package com.ecommerce.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "product-service")
public record ProductServiceProperties(String baseUrl, String validationPath, String reservationPath) {
}
