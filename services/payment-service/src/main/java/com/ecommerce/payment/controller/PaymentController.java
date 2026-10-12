package com.ecommerce.payment.controller;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.payment.dto.CancelPayOSPaymentRequest;
import com.ecommerce.payment.dto.CreatePayOSPaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final com.ecommerce.payment.config.PayOSProperties properties;

    public PaymentController(PaymentService paymentService, com.ecommerce.payment.config.PayOSProperties properties) {
        this.paymentService = paymentService; this.properties=properties;
    }

    @GetMapping("/capabilities")
    public ApiResponse<Object> capabilities(){return ApiResponse.ok(java.util.Map.of("payosEnabled",properties.enabled(),"codEnabled",true));}
    @PostMapping("/cod/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> createCod(@Valid @RequestBody CreatePayOSPaymentRequest request){return ApiResponse.ok(paymentService.createCodPayment(request));}
    @PostMapping("/payos/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> createPayOSPayment(
            @Valid @RequestBody CreatePayOSPaymentRequest request
    ) {
        return ApiResponse.ok(
                paymentService.createPayOSPayment(request),
                "PayOS payment link created."
        );
    }

    @PostMapping("/payos/webhook")
    public ApiResponse<PaymentResponse> payOSWebhook(
            @RequestBody Webhook webhook
    ) {
        return ApiResponse.ok(
                paymentService.handleWebhook(webhook),
                "Webhook verified and processed."
        );
    }

    @PostMapping("/payos/webhook/confirm")
    public ApiResponse<Object> confirmPayOSWebhook() {
        return ApiResponse.ok(
                paymentService.confirmWebhook(),
                "PayOS webhook confirmed."
        );
    }

    @PostMapping("/payos/{orderCode}/cancel")
    public ApiResponse<PaymentResponse> cancelPayOSPayment(
            @PathVariable("orderCode") Long orderCode,
            @RequestBody(required = false) CancelPayOSPaymentRequest request
    ) {
        return ApiResponse.ok(
                paymentService.cancel(orderCode, request),
                "Payment cancelled."
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getById(
            @PathVariable("id") UUID id
    ) {
        return ApiResponse.ok(paymentService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<PaymentResponse> getByOrderId(
            @PathVariable("orderId") UUID orderId
    ) {
        return ApiResponse.ok(paymentService.getByOrderId(orderId));
    }

    @GetMapping("/order-code/{orderCode}")
    public ApiResponse<PaymentResponse> getByOrderCode(
            @PathVariable("orderCode") Long orderCode
    ) {
        return ApiResponse.ok(
                paymentService.getByOrderCode(orderCode)
        );
    }
}
