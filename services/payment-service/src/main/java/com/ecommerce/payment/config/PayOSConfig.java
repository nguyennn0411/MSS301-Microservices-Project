package com.ecommerce.payment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "payos",
            name = "enabled",
            havingValue = "true"
    )
    PayOS payOS(PayOSProperties properties) {
        requireSecret(properties.clientId(), "PAYOS_CLIENT_ID");
        requireSecret(properties.apiKey(), "PAYOS_API_KEY");
        requireSecret(properties.checksumKey(), "PAYOS_CHECKSUM_KEY");

        return new PayOS(
                properties.clientId(),
                properties.apiKey(),
                properties.checksumKey()
        );
    }

    private void requireSecret(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    name + " is required when PAYOS_ENABLED=true"
            );
        }
    }
}
