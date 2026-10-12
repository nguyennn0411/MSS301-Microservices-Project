package com.ecommerce.order.client;

import com.ecommerce.order.config.ProductServiceProperties;
import com.ecommerce.order.dto.ProductReservationRequest;
import com.ecommerce.order.dto.ProductReservationResponse;
import com.ecommerce.order.exception.OrderIntegrationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class ProductGatewayClient {
    private final RestClient restClient;
    private final String reservationPath;

    public ProductGatewayClient(ProductServiceProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
        this.reservationPath = properties.reservationPath();
    }

    public ProductReservationResponse reserve(ProductReservationRequest request) {
        try {
            return restClient.post().uri(reservationPath).body(request).retrieve().body(ProductReservationResponse.class);
        } catch (RestClientException exception) {
            throw new OrderIntegrationException("Product validation or inventory reservation failed", exception);
        }
    }

    public void release(UUID orderId) {
        try {
            restClient.delete().uri(reservationPath + "/{orderId}", orderId).retrieve().toBodilessEntity();
        } catch (RestClientException exception) {
            throw new OrderIntegrationException("Could not release inventory for order " + orderId, exception);
        }
    }
}
