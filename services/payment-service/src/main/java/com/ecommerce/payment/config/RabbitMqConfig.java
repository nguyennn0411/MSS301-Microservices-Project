package com.ecommerce.payment.config;

import com.ecommerce.payment.messaging.PaymentMessaging;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    TopicExchange paymentEventsExchange() {
        return new TopicExchange(PaymentMessaging.EXCHANGE, true, false);
    }
}
