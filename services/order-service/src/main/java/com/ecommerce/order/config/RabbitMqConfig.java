package com.ecommerce.order.config;

import com.ecommerce.order.messaging.PaymentMessaging;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    TopicExchange paymentEventsExchange() {
        return new TopicExchange(PaymentMessaging.EXCHANGE, true, false);
    }

    @Bean
    Queue orderPaymentEventsQueue() {
        return new Queue(PaymentMessaging.ORDER_QUEUE, true);
    }

    @Bean
    Binding orderPaymentEventsBinding(
            Queue orderPaymentEventsQueue,
            TopicExchange paymentEventsExchange
    ) {
        return BindingBuilder
                .bind(orderPaymentEventsQueue)
                .to(paymentEventsExchange)
                .with(PaymentMessaging.ALL_PAYMENT_EVENTS);
    }
}
