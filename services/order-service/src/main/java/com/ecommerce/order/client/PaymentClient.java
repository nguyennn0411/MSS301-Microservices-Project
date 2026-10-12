package com.ecommerce.order.client;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.client.dto.CreatePaymentRequest;
import com.ecommerce.order.client.dto.PaymentResponse;
import com.ecommerce.order.config.PaymentServiceProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {

    private final RestClient restClient;
    private final PaymentServiceProperties properties;

    public PaymentClient(
            RestClient.Builder builder,
            PaymentServiceProperties properties
    ) {
        this.properties = properties;
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public boolean payosEnabled() {
        var result=restClient.get().uri("/api/payments/capabilities").retrieve().body(java.util.Map.class);
        return result!=null && result.get("data") instanceof java.util.Map<?,?> data && Boolean.TRUE.equals(data.get("payosEnabled"));
    }
    public PaymentResponse createPayment(String method, CreatePaymentRequest request) {
        ApiResponse<PaymentResponse> response = restClient.post()
                .uri("COD".equals(method)?"/api/payments/cod/create":properties.createPayosPath())
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalArgumentException(
                    response != null ? response.message() : "Payment service returned no data."
            );
        }

        return response.data();
    }

    public PaymentResponse cancelPayOSPayment(Long orderCode, String reason) {
        ApiResponse<PaymentResponse> response = restClient.post()
                .uri(properties.cancelPayosPath(), orderCode)
                .body(new CancelPaymentRequest(reason))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalArgumentException(
                    response != null ? response.message() : "Payment cancellation returned no data."
            );
        }

        return response.data();
    }

    private record CancelPaymentRequest(String reason) {
    }
}
