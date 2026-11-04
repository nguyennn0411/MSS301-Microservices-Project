package com.ecommerce.order.client.feign;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.client.PaymentClient.CancelPaymentRequest;
import com.ecommerce.order.client.dto.CreatePaymentRequest;
import com.ecommerce.order.client.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "payment-service", url = "${payment-service.base-url}")
public interface PaymentFeignClient {

    @GetMapping("/api/payments/capabilities")
    ApiResponse<Map<String, Object>> capabilities();

    @PostMapping("/api/payments/cod/create")
    ApiResponse<PaymentResponse> createCodPayment(@RequestBody CreatePaymentRequest request);

    @PostMapping("/api/payments/payos/create")
    ApiResponse<PaymentResponse> createPayOSPayment(@RequestBody CreatePaymentRequest request);

    @PostMapping("/api/payments/payos/{orderCode}/cancel")
    ApiResponse<PaymentResponse> cancelPayment(
            @PathVariable("orderCode") Long orderCode,
            @RequestBody CancelPaymentRequest request
    );
}
