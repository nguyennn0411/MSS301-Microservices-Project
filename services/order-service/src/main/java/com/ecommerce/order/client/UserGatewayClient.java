package com.ecommerce.order.client;

import com.ecommerce.order.config.UserServiceProperties;
import com.ecommerce.order.dto.UserValidationResponse;
import com.ecommerce.order.exception.OrderIntegrationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class UserGatewayClient {
    private final RestClient restClient;
    private final String validationPath;

    public UserGatewayClient(UserServiceProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
        this.validationPath = properties.validationPath();
    }

    public UserValidationResponse validate(UUID userId) {
        try {
            return restClient.get().uri(validationPath, userId).retrieve().body(UserValidationResponse.class);
        } catch (RestClientException exception) {
            throw new OrderIntegrationException("Could not validate user", exception);
        }
    }
}
