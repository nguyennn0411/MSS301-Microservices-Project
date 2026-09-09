package com.ecommerce.payment;

import com.ecommerce.common.events.PaymentSuccessEvent;
import com.ecommerce.payment.config.RabbitMqConfig;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.service.PaymentEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false"
        }
)
class PaymentEventPublisherIntegrationTest {

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishesPaymentSuccessToRabbitMq() {
        String queueName = "test.payment.success." + UUID.randomUUID();
        Queue queue = QueueBuilder.nonDurable(queueName).exclusive().autoDelete().build();
        TopicExchange exchange = new TopicExchange(RabbitMqConfig.ECOMMERCE_EXCHANGE, true, false);
        Binding binding = BindingBuilder.bind(queue)
                .to(exchange)
                .with(RabbitMqConfig.PAYMENT_SUCCESS_ROUTING_KEY);

        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);

        try {
            UUID orderId = UUID.randomUUID();
            long orderCode = System.currentTimeMillis() * 1_000L + 947L;
            String paymentLinkId = "rabbit-test-link-" + UUID.randomUUID();
            String providerReference = "PAYOS-RABBIT-TEST-" + UUID.randomUUID();
            Payment payment = new Payment(
                    orderId,
                    UUID.randomUUID(),
                    orderCode,
                    new BigDecimal("99000"),
                    "VND",
                    "Rabbit Integration Test",
                    "rabbit-integration-test@example.com"
            );
            payment.attachCheckoutInfo(paymentLinkId, "https://pay.payos.vn/test", "test-qr");
            payment.markSuccess();

            paymentEventPublisher.publishPaymentSuccess(payment, providerReference);

            Object received = rabbitTemplate.receiveAndConvert(queueName, 5_000L);
            assertThat(received).isInstanceOf(PaymentSuccessEvent.class);
            PaymentSuccessEvent event = (PaymentSuccessEvent) received;
            assertThat(event.orderId()).isEqualTo(orderId);
            assertThat(event.orderCode()).isEqualTo(orderCode);
            assertThat(event.paymentLinkId()).isEqualTo(paymentLinkId);
            assertThat(event.providerReference()).isEqualTo(providerReference);
            assertThat(event.amount()).isEqualByComparingTo("99000");
            assertThat(event.currency()).isEqualTo("VND");
        } finally {
            amqpAdmin.deleteQueue(queueName);
        }
    }
}
