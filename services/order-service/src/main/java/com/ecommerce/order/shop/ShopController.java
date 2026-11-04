package com.ecommerce.order.shop;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.shop.domain.OrderEntity;
import com.ecommerce.order.client.dto.ProductValidationResponse;
import com.ecommerce.order.shop.dto.CreateOrderItemRequest;
import com.ecommerce.order.shop.dto.CreateOrderRequest;
import com.ecommerce.order.shop.dto.OrderResponse;
import com.ecommerce.order.shop.repository.ShopOrderRepository;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.client.ProductCatalogClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@Profile("shop")
@RequestMapping("/api/shop")
public class ShopController {

    private static final String SHOP_SESSION_HEADER = "X-Shop-Session";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final ShopOrderRepository orderRepository;
    private final ShopSettings shopSettings;
    private final ProductCatalogClient productCatalogClient;

    public ShopController(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            OrderService orderService,
            ShopOrderRepository orderRepository,
            ShopSettings shopSettings,
            ProductCatalogClient productCatalogClient
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.shopSettings = shopSettings;
        this.productCatalogClient = productCatalogClient;
    }

    public record CartRequest(
            @NotNull @Size(max = 50) List<@Valid CreateOrderItemRequest> items
    ) {
    }

    public record Checkout(
            @NotBlank @Size(max = 150) String buyerName,
            @NotBlank @Email @Size(max = 200) String buyerEmail,
            @NotBlank @Pattern(regexp = "[0-9+ ()-]{9,16}") String buyerPhone,
            @NotBlank @Size(max = 500) String shippingAddress,
            @Size(max = 500) String customerNote,
            @NotNull @Pattern(regexp = "COD|PAYOS") String paymentMethod,
            @NotNull UUID requestKey
    ) {
    }

    public record Cancel(@Size(max = 500) String reason) {
    }

    @GetMapping("/settings")
    public ApiResponse<Map<String, BigDecimal>> settings() {
        return ApiResponse.ok(Map.of("shippingFee", shopSettings.shippingFee()));
    }

    @GetMapping("/cart")
    public ApiResponse<List<CreateOrderItemRequest>> cart(
            @RequestHeader(SHOP_SESSION_HEADER) UUID shopSessionId
    ) {
        return ApiResponse.ok(readCart(shopSessionId));
    }

    @PutMapping("/cart")
    @Transactional
    public ApiResponse<List<CreateOrderItemRequest>> updateCart(
            @RequestHeader(SHOP_SESSION_HEADER) UUID shopSessionId,
            @Valid @RequestBody CartRequest request
    ) {
        readCartForUpdate(shopSessionId);
        validateCartItems(request.items());
        writeCart(shopSessionId, request.items());

        return ApiResponse.ok(request.items());
    }

    @PostMapping("/checkout")
    @Transactional
    public ApiResponse<OrderResponse> checkout(
            @RequestHeader(SHOP_SESSION_HEADER) UUID shopSessionId,
            @Valid @RequestBody Checkout request
    ) {
        List<CreateOrderItemRequest> items = readCartForUpdate(shopSessionId);
        Optional<OrderEntity> existingOrder = orderRepository.findByUserIdAndRequestKey(
                shopSessionId,
                request.requestKey()
        );

        if (existingOrder.isPresent()) {
            return ApiResponse.ok(orderService.toResponse(existingOrder.get()));
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống.");
        }

        OrderResponse order = orderService.create(new CreateOrderRequest(
                shopSessionId,
                request.buyerName(),
                request.buyerEmail(),
                items,
                request.buyerPhone(),
                request.shippingAddress(),
                request.customerNote(),
                request.paymentMethod(),
                request.requestKey()
        ));

        writeCart(shopSessionId, List.of());
        return ApiResponse.ok(order);
    }

    @GetMapping("/orders")
    public ApiResponse<List<OrderResponse>> history(
            @RequestHeader(SHOP_SESSION_HEADER) UUID shopSessionId
    ) {
        List<OrderResponse> history = orderRepository
                .findByUserIdOrderByCreatedAtDesc(shopSessionId)
                .stream()
                .map(orderService::toResponse)
                .toList();

        return ApiResponse.ok(history);
    }

    @GetMapping("/orders/{code}")
    public ApiResponse<OrderResponse> find(
            @RequestHeader(SHOP_SESSION_HEADER) UUID shopSessionId,
            @PathVariable("code") Long orderCode
    ) {
        return ApiResponse.ok(orderService.getById(findOwnedOrderId(shopSessionId, orderCode)));
    }

    @PostMapping("/orders/{code}/cancel")
    @Transactional
    public ApiResponse<OrderResponse> cancel(
            @RequestHeader(SHOP_SESSION_HEADER) UUID shopSessionId,
            @PathVariable("code") Long orderCode,
            @Valid @RequestBody Cancel request
    ) {
        UUID orderId = findOwnedOrderId(shopSessionId, orderCode);
        jdbcTemplate.queryForList(
                "SELECT id FROM orders WHERE id = ? FOR UPDATE",
                orderId.toString()
        );

        return ApiResponse.ok(orderService.cancel(orderId, request.reason()));
    }

    private void validateCartItems(List<CreateOrderItemRequest> items) {
        boolean missingVariant = items.stream()
                .anyMatch(item -> item.variantId() == null);
        if (missingVariant) {
            throw new IllegalArgumentException("Vui lòng chọn size và màu.");
        }

        long uniqueVariantCount = items.stream()
                .map(CreateOrderItemRequest::variantId)
                .distinct()
                .count();
        if (uniqueVariantCount != items.size()) {
            throw new IllegalArgumentException("Mỗi biến thể chỉ được xuất hiện một lần.");
        }

        if (items.isEmpty()) {
            return;
        }

        ProductValidationResponse validation = productCatalogClient.validate(items);
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.message());
        }
    }

    private void ensureCartExists(UUID shopSessionId) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO shop_carts(user_id, items_json) VALUES (?, '[]')",
                shopSessionId.toString()
        );
    }

    private List<CreateOrderItemRequest> readCart(UUID shopSessionId) {
        return readCart(shopSessionId, "");
    }

    private List<CreateOrderItemRequest> readCartForUpdate(UUID shopSessionId) {
        return readCart(shopSessionId, " FOR UPDATE");
    }

    private List<CreateOrderItemRequest> readCart(UUID shopSessionId, String lockClause) {
        ensureCartExists(shopSessionId);

        String itemsJson = jdbcTemplate.queryForObject(
                "SELECT items_json FROM shop_carts WHERE user_id = ?" + lockClause,
                String.class,
                shopSessionId.toString()
        );

        try {
            return objectMapper.readValue(itemsJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot read cart", exception);
        }
    }

    private void writeCart(UUID shopSessionId, List<CreateOrderItemRequest> items) {
        try {
            String itemsJson = objectMapper.writeValueAsString(items);
            jdbcTemplate.update(
                    "UPDATE shop_carts "
                            + "SET items_json = ?, updated_at = CURRENT_TIMESTAMP(6) "
                            + "WHERE user_id = ?",
                    itemsJson,
                    shopSessionId.toString()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot write cart", exception);
        }
    }

    private UUID findOwnedOrderId(UUID shopSessionId, Long orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .filter(order -> order.getUserId().equals(shopSessionId))
                .map(OrderEntity::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng trong phiên mua sắm này."
                ));
    }
}
