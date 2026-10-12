package com.ecommerce.order.messaging;

import com.ecommerce.common.events.PaymentCancelledEvent;
import com.ecommerce.common.events.PaymentFailedEvent;
import com.ecommerce.common.events.PaymentRefundedEvent;
import com.ecommerce.common.events.PaymentSuccessEvent;
import com.ecommerce.order.shop.domain.OrderEntity;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("shop")
public class PaymentEventListener {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final com.ecommerce.order.shop.repository.ShopOrderRepository orders;

    public PaymentEventListener(
            ObjectMapper objectMapper,
            OrderService orderService, com.ecommerce.order.shop.repository.ShopOrderRepository orders
    ) {
        this.objectMapper = objectMapper;
        this.orderService = orderService; this.orders=orders;
    }

    @RabbitListener(queues = PaymentMessaging.ORDER_QUEUE)
    public void consume(Message message) throws IOException {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        switch (routingKey) {
            case PaymentMessaging.PAYMENT_SUCCESS ->
                    handleSuccess(read(message, PaymentSuccessEvent.class));

            case PaymentMessaging.PAYMENT_FAILED ->
                    handleFailed(read(message, PaymentFailedEvent.class));

            case PaymentMessaging.PAYMENT_CANCELLED ->
                    handleCancelled(read(message, PaymentCancelledEvent.class));

            case PaymentMessaging.PAYMENT_REFUNDED ->
                    handleRefunded(read(message, PaymentRefundedEvent.class));

            case PaymentMessaging.PAYMENT_CREATED -> {
                // Order already receives the create-payment REST response.
                // The event is intentionally ignored here.
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported payment routing key: " + routingKey
            );
        }
    }

    private void handleSuccess(PaymentSuccessEvent event) {
        OrderEntity order = orders.findByOrderCode(event.orderCode()).orElse(null);
        if(order==null) return;
        if (!order.getId().equals(event.orderId()) || !"PAYOS".equals(order.getPaymentMethod())) return;
        order.markPaid(event.paymentId(), event.paymentLinkId());
        orderService.save(order);
    }

    private void handleFailed(PaymentFailedEvent event) {
        OrderEntity order = orders.findByOrderCode(event.orderCode()).orElse(null);
        if(order==null) return;
        if (!order.getId().equals(event.orderId()) || !"PAYOS".equals(order.getPaymentMethod())) return;
        order.markPaymentFailed(event.reason());
        orderService.save(order);
    }

    private void handleCancelled(PaymentCancelledEvent event) {
        OrderEntity order = orders.findByOrderCode(event.orderCode()).orElse(null);
        if(order==null) return;
        if (!order.getId().equals(event.orderId())) return;

        if (order.getStatus().name().equals("PAID")) {
            return;
        }

        order.markCancelled(event.reason());
        orderService.save(order);
    }

    private void handleRefunded(PaymentRefundedEvent event) {
        OrderEntity order = orders.findByOrderCode(event.orderCode()).orElse(null);
        if(order==null) return;
        if (!order.getId().equals(event.orderId()) || !"PAYOS".equals(order.getPaymentMethod())) return;
        order.markRefunded();
        orderService.save(order);
    }

    private <T> T read(Message message, Class<T> type) throws IOException {
        return objectMapper.readValue(message.getBody(), type);
    }
}
