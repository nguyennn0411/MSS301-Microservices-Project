package com.ecommerce.payment.messaging;

import com.ecommerce.common.events.*;
import com.ecommerce.payment.domain.PaymentEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PaymentEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishCreated(PaymentEntity payment) {
        publish(
                PaymentMessaging.PAYMENT_CREATED,
                new PaymentCreatedEvent(
                        "PAYMENT_CREATED",
                        payment.getOrderId(),
                        payment.getOrderCode(),
                        payment.getId(),
                        payment.getUserId(),
                        payment.getBuyerName(),
                        payment.getBuyerEmail(),
                        payment.getPaymentLinkId(),
                        payment.getCheckoutUrl(),
                        payment.getQrCode(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getProvider(),
                        Instant.now()
                )
        );
    }

    public void publishSuccess(PaymentEntity payment) {
        publish(
                PaymentMessaging.PAYMENT_SUCCESS,
                new PaymentSuccessEvent(
                        "PAYMENT_SUCCESS",
                        payment.getOrderId(),
                        payment.getOrderCode(),
                        payment.getId(),
                        payment.getUserId(),
                        payment.getBuyerName(),
                        payment.getBuyerEmail(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getProvider(),
                        payment.getPaymentLinkId(),
                        payment.getProviderReference(),
                        Instant.now()
                )
        );
    }

    public void publishFailed(PaymentEntity payment) {
        publish(
                PaymentMessaging.PAYMENT_FAILED,
                new PaymentFailedEvent(
                        "PAYMENT_FAILED",
                        payment.getOrderId(),
                        payment.getOrderCode(),
                        payment.getId(),
                        payment.getUserId(),
                        payment.getBuyerName(),
                        payment.getBuyerEmail(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getProvider(),
                        payment.getStatusReason(),
                        Instant.now()
                )
        );
    }

    public void publishCancelled(PaymentEntity payment) {
        publish(
                PaymentMessaging.PAYMENT_CANCELLED,
                new PaymentCancelledEvent(
                        "PAYMENT_CANCELLED",
                        payment.getOrderId(),
                        payment.getOrderCode(),
                        payment.getId(),
                        payment.getUserId(),
                        payment.getBuyerName(),
                        payment.getBuyerEmail(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getProvider(),
                        payment.getStatusReason(),
                        Instant.now()
                )
        );
    }

    public void publishRefunded(PaymentEntity payment) {
        publish(
                PaymentMessaging.PAYMENT_REFUNDED,
                new PaymentRefundedEvent(
                        "PAYMENT_REFUNDED",
                        payment.getOrderId(),
                        payment.getOrderCode(),
                        payment.getId(),
                        payment.getUserId(),
                        payment.getBuyerName(),
                        payment.getBuyerEmail(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getProvider(),
                        payment.getPaymentLinkId(),
                        payment.getProviderReference(),
                        Instant.now()
                )
        );
    }

    private void publish(String routingKey, Object event) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(event);

            Message message = MessageBuilder
                    .withBody(body)
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setHeader("eventType", routingKey)
                    .build();

            rabbitTemplate.send(
                    PaymentMessaging.EXCHANGE,
                    routingKey,
                    message
            );
        } catch (JsonProcessingException ex) {
            log.error("Cannot serialize payment event {}", routingKey, ex);
            throw new IllegalStateException(
                    "Cannot serialize payment event " + routingKey,
                    ex
            );
        }
    }
}
