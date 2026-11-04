package com.ecommerce.order.client.feign;

import com.ecommerce.order.dto.ProductReservationRequest;
import com.ecommerce.order.dto.ProductReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "product-catalog-service", contextId = "productGatewayFeignClient",
        url = "${product-service.base-url}")
public interface ProductGatewayFeignClient {

    @PostMapping("${product-service.reservation-path}")
    ProductReservationResponse reserve(@RequestBody ProductReservationRequest request);

    @DeleteMapping("${product-service.reservation-path}/{orderId}")
    void release(@PathVariable("orderId") UUID orderId);
}
