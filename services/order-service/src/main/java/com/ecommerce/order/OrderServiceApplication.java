package com.ecommerce.order;

import com.ecommerce.order.config.ProductServiceProperties;
import com.ecommerce.order.config.UserServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableScheduling
@EnableConfigurationProperties({
        ProductServiceProperties.class,
        UserServiceProperties.class
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
