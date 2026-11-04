package com.ecommerce.order;

import com.ecommerce.order.config.ProductServiceProperties;
import com.ecommerce.order.config.PaymentServiceProperties;
import com.ecommerce.order.config.UserServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableFeignClients(basePackages = "com.ecommerce.order.client.feign")
@EnableConfigurationProperties({
        PaymentServiceProperties.class,
        ProductServiceProperties.class,
        UserServiceProperties.class
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
