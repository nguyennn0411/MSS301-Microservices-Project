package com.ecommerce.order.client.feign;

import com.ecommerce.order.dto.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.base-url}")
public interface UserGatewayFeignClient {

    @GetMapping("${user-service.validation-path}")
    UserValidationResponse validate(@PathVariable("userId") UUID userId);
}
