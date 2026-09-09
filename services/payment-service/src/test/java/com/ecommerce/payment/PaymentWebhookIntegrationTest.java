package com.ecommerce.payment;

import com.ecommerce.payment.dto.PayosWebhookData;
import com.ecommerce.payment.dto.PayosWebhookRequest;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentTransaction;
import com.ecommerce.payment.enums.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.PaymentEventPublisher;
import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.payment.service.PayosPaymentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false"
        }
)
@Transactional
class PaymentWebhookIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private PayosPaymentService payosPaymentService;

    @MockitoBean
    private PaymentEventPublisher paymentEventPublisher;

    @Test
    void successfulWebhookStoresProviderReferenceExactlyOnce() {
        long orderCode = System.currentTimeMillis() * 1_000L + 731L;
        String paymentLinkId = "integration-test-link-" + UUID.randomUUID();
        String providerReference = "PAYOS-TEST-REFERENCE-" + UUID.randomUUID();

        Payment payment = new Payment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                orderCode,
                new BigDecimal("99000"),
                "VND",
                "Integration Test",
                "integration-test@example.com"
        );
        payment.attachCheckoutInfo(paymentLinkId, "https://pay.payos.vn/test", "test-qr");
        payment = paymentRepository.saveAndFlush(payment);

        PayosWebhookRequest webhook = successfulWebhook(
                orderCode,
                paymentLinkId,
                providerReference
        );
        when(payosPaymentService.verifyWebhookSignature(any(PayosWebhookRequest.class)))
                .thenReturn(true);

        Map<String, Object> firstResponse = paymentService.handleWebhook(webhook);
        Map<String, Object> duplicateResponse = paymentService.handleWebhook(webhook);
        entityManager.flush();
        entityManager.clear();

        Payment persistedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        List<PaymentTransaction> transactions = entityManager.createQuery(
                        "select tx from PaymentTransaction tx where tx.payment.id = :paymentId",
                        PaymentTransaction.class
                )
                .setParameter("paymentId", payment.getId())
                .getResultList();

        assertThat(firstResponse.get("status")).isEqualTo("SUCCESS");
        assertThat(duplicateResponse.get("message"))
                .isEqualTo("Webhook ignored because payment is already finalized");
        assertThat(persistedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(persistedPayment.getPaidAt()).isNotNull();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.getFirst().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(transactions.getFirst().getProviderReference()).isEqualTo(providerReference);
        assertThat(transactions.getFirst().getProviderResponse()).contains(providerReference);
        verify(paymentEventPublisher, times(1))
                .publishPaymentSuccess(any(Payment.class), eq(providerReference));
    }

    private PayosWebhookRequest successfulWebhook(long orderCode,
                                                   String paymentLinkId,
                                                   String providerReference) {
        return new PayosWebhookRequest(
                "00",
                "success",
                true,
                new PayosWebhookData(
                        orderCode,
                        99_000L,
                        "Integration test payment",
                        "123456789",
                        providerReference,
                        "2026-07-19 13:45:00",
                        "VND",
                        paymentLinkId,
                        "00",
                        "success",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                ),
                "verified-by-mock"
        );
    }
}
