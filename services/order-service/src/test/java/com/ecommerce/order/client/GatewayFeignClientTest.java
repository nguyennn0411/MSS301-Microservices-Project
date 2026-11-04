package com.ecommerce.order.client;

import com.ecommerce.order.client.feign.ProductGatewayFeignClient;
import com.ecommerce.order.client.feign.UserGatewayFeignClient;
import com.ecommerce.order.dto.ProductReservationRequest;
import com.ecommerce.order.dto.ProductReservationResponse;
import com.ecommerce.order.dto.UserValidationResponse;
import com.ecommerce.order.exception.OrderIntegrationException;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayFeignClientTest {
    @Mock UserGatewayFeignClient userFeign;
    @Mock ProductGatewayFeignClient productFeign;

    @Test
    void delegatesUserValidationAndPreservesIntegrationError() {
        var client = new UserGatewayClient(userFeign);
        UUID userId = UUID.randomUUID();
        var response = new UserValidationResponse(userId, true, "active");
        when(userFeign.validate(userId)).thenReturn(response);

        assertThat(client.validate(userId)).isSameAs(response);
        verify(userFeign).validate(userId);

        FeignException failure = httpError(Request.HttpMethod.GET);
        when(userFeign.validate(userId)).thenThrow(failure);
        assertThatThrownBy(() -> client.validate(userId))
                .isInstanceOf(OrderIntegrationException.class)
                .hasMessage("Could not validate user")
                .hasCause(failure);
    }

    @Test
    void delegatesReservationAndReleaseAndPreservesIntegrationErrors() {
        var client = new ProductGatewayClient(productFeign);
        UUID orderId = UUID.randomUUID();
        var request = new ProductReservationRequest(orderId, List.of());
        var response = new ProductReservationResponse(orderId, true, "reserved", List.of());
        when(productFeign.reserve(request)).thenReturn(response);

        assertThat(client.reserve(request)).isSameAs(response);
        client.release(orderId);
        verify(productFeign).reserve(request);
        verify(productFeign).release(orderId);

        FeignException reserveFailure = httpError(Request.HttpMethod.POST);
        when(productFeign.reserve(request)).thenThrow(reserveFailure);
        assertThatThrownBy(() -> client.reserve(request))
                .isInstanceOf(OrderIntegrationException.class)
                .hasMessage("Product validation or inventory reservation failed")
                .hasCause(reserveFailure);

        FeignException releaseFailure = httpError(Request.HttpMethod.DELETE);
        org.mockito.Mockito.doThrow(releaseFailure).when(productFeign).release(orderId);
        assertThatThrownBy(() -> client.release(orderId))
                .isInstanceOf(OrderIntegrationException.class)
                .hasMessage("Could not release inventory for order " + orderId)
                .hasCause(releaseFailure);
    }

    private static FeignException httpError(Request.HttpMethod method) {
        Request request = Request.create(method, "http://localhost/test", Map.of(), null, null, null);
        return FeignException.errorStatus("test", feign.Response.builder()
                .status(503).reason("Unavailable").request(request).build());
    }
}
