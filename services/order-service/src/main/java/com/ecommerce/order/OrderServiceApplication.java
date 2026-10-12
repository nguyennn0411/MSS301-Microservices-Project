package com.ecommerce.order;

import com.ecommerce.order.config.PaymentServiceProperties;
import com.ecommerce.order.config.ProductServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableConfigurationProperties({
        PaymentServiceProperties.class,
        ProductServiceProperties.class
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
