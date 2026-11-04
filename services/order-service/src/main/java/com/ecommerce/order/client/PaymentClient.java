package com.ecommerce.order.client;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.client.feign.PaymentFeignClient;
import com.ecommerce.order.client.dto.CreatePaymentRequest;
import com.ecommerce.order.client.dto.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentClient {

    private final PaymentFeignClient paymentFeignClient;

    public PaymentClient(PaymentFeignClient paymentFeignClient) {
        this.paymentFeignClient = paymentFeignClient;
    }

    public boolean payosEnabled() {
        ApiResponse<Map<String, Object>> response = paymentFeignClient.capabilities();
        return response != null
                && response.success()
                && response.data() != null
                && Boolean.TRUE.equals(response.data().get("payosEnabled"));
    }

    public PaymentResponse createPayment(String method, CreatePaymentRequest request) {
        ApiResponse<PaymentResponse> response = "COD".equals(method)
                ? paymentFeignClient.createCodPayment(request)
                : paymentFeignClient.createPayOSPayment(request);
        return requireData(response, "Payment service returned no data.");
    }

    public PaymentResponse cancelPayOSPayment(Long orderCode, String reason) {
        ApiResponse<PaymentResponse> response = paymentFeignClient.cancelPayment(
                orderCode,
                new CancelPaymentRequest(reason)
        );
        return requireData(response, "Payment cancellation returned no data.");
    }

    private PaymentResponse requireData(ApiResponse<PaymentResponse> response, String fallbackMessage) {
        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalArgumentException(
                    response != null ? response.message() : fallbackMessage
            );
        }
        return response.data();
    }

    public record CancelPaymentRequest(String reason) {
    }
}
