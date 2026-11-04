package com.ecommerce.order.client;

import com.ecommerce.order.client.feign.PaymentFeignClient;
import com.ecommerce.order.client.feign.ProductCatalogFeignClient;
import com.ecommerce.order.client.feign.ProductGatewayFeignClient;
import com.ecommerce.order.client.feign.UserGatewayFeignClient;
import com.ecommerce.order.dto.ProductReservationRequest;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GatewayFeignHttpTest.TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.cloud.config.enabled=false", "eureka.client.enabled=false", "debug=false"})
class GatewayFeignHttpTest {
    private static final HttpServer server = startServer();
    private static final AtomicReference<String> lastRequest = new AtomicReference<>();
    private static final AtomicReference<String> lastBody = new AtomicReference<>();

    @Autowired UserGatewayFeignClient users;
    @Autowired ProductGatewayFeignClient products;

    @DynamicPropertySource
    static void endpoints(DynamicPropertyRegistry registry) {
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        registry.add("user-service.base-url", () -> baseUrl);
        registry.add("product-service.base-url", () -> baseUrl);
        registry.add("user-service.validation-path", () -> "/custom/users/{userId}/validation");
        registry.add("product-service.reservation-path", () -> "/custom/reservations");
    }

    @Test
    void usesConfiguredPathsAndSerializesRequests() {
        UUID userId = UUID.randomUUID();
        assertThat(users.validate(userId).userId()).isEqualTo(userId);
        assertThat(lastRequest.get()).isEqualTo("GET /custom/users/" + userId + "/validation");

        UUID orderId = UUID.randomUUID();
        assertThat(products.reserve(new ProductReservationRequest(orderId, List.of())).reserved()).isTrue();
        assertThat(lastRequest.get()).isEqualTo("POST /custom/reservations");
        assertThat(lastBody.get()).contains("\"orderId\":\"" + orderId + "\"");

        products.release(orderId);
        assertThat(lastRequest.get()).isEqualTo("DELETE /custom/reservations/" + orderId);
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    private static HttpServer startServer() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            httpServer.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                lastRequest.set(exchange.getRequestMethod() + " " + path);
                lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String response;
                if (path.endsWith("/validation")) {
                    String userId = path.split("/")[3];
                    response = "{\"userId\":\"" + userId + "\",\"valid\":true,\"message\":\"active\"}";
                } else if (exchange.getRequestMethod().equals("POST")) {
                    String orderId = lastBody.get().split("\"orderId\":\"")[1].split("\"")[0];
                    response = "{\"orderId\":\"" + orderId + "\",\"reserved\":true,\"message\":\"reserved\",\"items\":[]}";
                } else {
                    response = "";
                }
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
                exchange.sendResponseHeaders(response.isEmpty() ? 204 : 200, response.isEmpty() ? -1 : bytes.length);
                if (bytes.length > 0) exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            httpServer.start();
            return httpServer;
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
    @EnableFeignClients(clients = {UserGatewayFeignClient.class, ProductGatewayFeignClient.class,
            ProductCatalogFeignClient.class, PaymentFeignClient.class})
    static class TestApplication {
    }
}
