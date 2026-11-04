package com.ecommerce.order.client;

import com.ecommerce.order.client.feign.UserGatewayFeignClient;
import com.ecommerce.order.dto.UserValidationResponse;
import com.ecommerce.order.exception.OrderIntegrationException;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserGatewayClient {
    private final UserGatewayFeignClient feignClient;

    public UserGatewayClient(UserGatewayFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    public UserValidationResponse validate(UUID userId) {
        try {
            return feignClient.validate(userId);
        } catch (FeignException exception) {
            throw new OrderIntegrationException("Could not validate user", exception);
        }
    }
}
