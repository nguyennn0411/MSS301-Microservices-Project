package com.ecommerce.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user-service")
public record UserServiceProperties(String baseUrl, String validationPath) {
}
