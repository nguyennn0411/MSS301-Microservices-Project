package com.ecommerce.order.application;

import com.ecommerce.order.client.ProductGatewayClient;
import com.ecommerce.order.client.UserGatewayClient;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.CreateOrderItemRequest;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.ProductReservationItemResponse;
import com.ecommerce.order.dto.ProductReservationResponse;
import com.ecommerce.order.dto.UserValidationResponse;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock UserGatewayClient userGatewayClient;
    @Mock ProductGatewayClient productGatewayClient;
    @InjectMocks OrderService orderService;

    @Test
    void validatesUserAndReservesInventoryBeforeCreatingOrder() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        when(userGatewayClient.validate(userId)).thenReturn(new UserValidationResponse(userId, true, "User is active"));
        when(productGatewayClient.reserve(any())).thenAnswer(invocation -> {
            var request = invocation.getArgument(0, com.ecommerce.order.dto.ProductReservationRequest.class);
            return new ProductReservationResponse(request.orderId(), true, "Inventory reserved", List.of(
                    new ProductReservationItemResponse(productId, "Essential T-Shirt", variantId,
                            "M", "Black", new BigDecimal("1000"), 2)));
        });
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.create(new CreateOrderRequest(userId, "Shopper", "shopper@example.com",
                "Delivery", "VND", new BigDecimal("50"), List.of(
                new CreateOrderItemRequest(productId, variantId, "M", "Black", 2, new BigDecimal("1000")))));

        assertThat(response.status()).isEqualTo("PAYMENT_PENDING");
        assertThat(response.totalAmount()).isEqualByComparingTo("2050");
        assertThat(response.items()).hasSize(1);
        verify(userGatewayClient).validate(userId);
        verify(productGatewayClient).reserve(any());
    }
}
