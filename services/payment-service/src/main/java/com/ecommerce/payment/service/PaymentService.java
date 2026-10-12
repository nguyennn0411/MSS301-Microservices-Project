package com.ecommerce.payment.service;

import com.ecommerce.payment.domain.PaymentEntity;
import com.ecommerce.payment.domain.PaymentStatus;
import com.ecommerce.payment.dto.CancelPayOSPaymentRequest;
import com.ecommerce.payment.dto.CreatePayOSPaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.gateway.PayOSGateway;
import com.ecommerce.payment.messaging.PaymentEventPublisher;
import com.ecommerce.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PayOSGateway payOSGateway;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            PayOSGateway payOSGateway,
            PaymentEventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.payOSGateway = payOSGateway;
        this.eventPublisher = eventPublisher;
    }

    public PaymentResponse createPayOSPayment(
            CreatePayOSPaymentRequest request
    ) {
        PaymentEntity existing = paymentRepository
                .findByOrderId(request.orderId())
                .orElse(null);

        if (existing != null) {
            validateIdempotentRequest(existing, request);
            if (!"PAYOS".equals(existing.getProvider())) {
                throw new IllegalArgumentException("Payment method conflict.");
            }
            return toResponse(existing);
        }

        validateCurrency(request.currency());

        PaymentEntity payment = PaymentEntity.create(
                request.orderId(),
                request.orderCode(),
                request.userId(),
                request.buyerName().trim(),
                request.buyerEmail().trim(),
                request.amount(),
                request.currency().trim().toUpperCase()
        );

        paymentRepository.saveAndFlush(payment);

        try {
            long payOSAmount = toPayOSAmount(request.amount());

            CreatePaymentLinkResponse response =
                    payOSGateway.createPaymentLink(
                            request.orderCode(),
                            payOSAmount
                    );

            payment.attachPaymentLink(
                    response.getPaymentLinkId(),
                    response.getCheckoutUrl(),
                    response.getQrCode()
            );

            paymentRepository.save(payment);
            publishSafely(() -> eventPublisher.publishCreated(payment));

            return toResponse(payment);
        } catch (RuntimeException ex) {
            payment.markFailed(ex.getMessage());
            paymentRepository.save(payment);
            publishSafely(() -> eventPublisher.publishFailed(payment));
            throw ex;
        }
    }

    public PaymentResponse createCodPayment(CreatePayOSPaymentRequest request) {
        PaymentEntity existing=paymentRepository.findByOrderId(request.orderId()).orElse(null);
        if(existing!=null){validateIdempotentRequest(existing,request);if(!"COD".equals(existing.getProvider()))throw new IllegalArgumentException("Payment method conflict");return toResponse(existing);}
        validateCurrency(request.currency());
        PaymentEntity payment=PaymentEntity.create(request.orderId(),request.orderCode(),request.userId(),request.buyerName().trim(),request.buyerEmail().trim(),request.amount(),"VND");
        payment.useCod();paymentRepository.saveAndFlush(payment);
        return toResponse(payment);
    }
    public PaymentResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public PaymentResponse getByOrderId(UUID orderId) {
        return toResponse(
                paymentRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Payment not found for order: " + orderId
                        ))
        );
    }

    public PaymentResponse getByOrderCode(Long orderCode) {
        return toResponse(findByOrderCode(orderCode));
    }

    public PaymentResponse cancel(
            Long orderCode,
            CancelPayOSPaymentRequest request
    ) {
        PaymentEntity payment = findByOrderCode(orderCode);

        if (payment.getStatus() == PaymentStatus.PAID
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException(
                    "Paid or refunded payment cannot be cancelled."
            );
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return toResponse(payment);
        }

        String reason = request == null
                || request.reason() == null
                || request.reason().isBlank()
                ? "Cancelled by customer"
                : request.reason().trim();

        if (!"COD".equals(payment.getProvider()) && payment.getPaymentLinkId()!=null) payOSGateway.cancelPaymentLink(orderCode, reason);

        payment.markCancelled(reason);
        paymentRepository.save(payment);
        publishSafely(() -> eventPublisher.publishCancelled(payment));

        return toResponse(payment);
    }

    public PaymentResponse handleWebhook(Webhook webhook) {
        WebhookData data = payOSGateway.verifyWebhook(webhook);
        PaymentEntity payment = findByOrderCode(data.getOrderCode());

        if (!"PAYOS".equals(payment.getProvider())) {
            throw new IllegalArgumentException("PayOS webhook cannot update a COD payment.");
        }
        if (payment.getStatus() == PaymentStatus.CANCELLED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            return toResponse(payment);
        }

        if ("00".equals(data.getCode())) {
            if (payment.getStatus() != PaymentStatus.PAID) {
                payment.markPaid(data.getReference());
                paymentRepository.save(payment);
                publishSafely(() -> eventPublisher.publishSuccess(payment));
            }
        } else {
            if (payment.getStatus() != PaymentStatus.PAID) {
                String reason = data.getDesc() != null
                        ? data.getDesc()
                        : "PayOS webhook reported failure.";
                payment.markFailed(reason);
                paymentRepository.save(payment);
                publishSafely(() -> eventPublisher.publishFailed(payment));
            }
        }

        return toResponse(payment);
    }

    public Object confirmWebhook() {
        return payOSGateway.confirmWebhook();
    }

    private PaymentEntity findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + id
                ));
    }

    private PaymentEntity findByOrderCode(Long orderCode) {
        return paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found for orderCode: " + orderCode
                ));
    }

    private void validateIdempotentRequest(
            PaymentEntity existing,
            CreatePayOSPaymentRequest request
    ) {
        if (!existing.getOrderCode().equals(request.orderCode())
                || !existing.getUserId().equals(request.userId())
                || existing.getAmount().compareTo(request.amount()) != 0
                || !existing.getCurrency().equalsIgnoreCase(request.currency())) {
            throw new IllegalArgumentException(
                    "Payment already exists with different order data."
            );
        }
    }

    private void validateCurrency(String currency) {
        if (!"VND".equalsIgnoreCase(currency)) {
            throw new IllegalArgumentException(
                    "This PayOS integration currently supports VND only."
            );
        }
    }

    private long toPayOSAmount(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "PayOS VND amount must be a whole number.",
                    ex
            );
        }
    }

    private void publishSafely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            // DB state remains the source of truth.
            // For a production system, use the Outbox Pattern.
            log.error("Payment event publish failed", ex);
        }
    }

    public PaymentResponse toResponse(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getOrderCode(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProvider(),
                payment.getStatus(),
                payment.getPaymentLinkId(),
                payment.getCheckoutUrl(),
                payment.getQrCode(),
                payment.getProviderReference(),
                payment.getStatusReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
