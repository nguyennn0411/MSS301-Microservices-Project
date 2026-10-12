package com.ecommerce.order.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
public record CreateOrderRequest(
 @NotNull UUID userId,
 @NotBlank @Size(max=150) String buyerName,
 @NotBlank @Email @Size(max=200) String buyerEmail,
 @NotEmpty List<@Valid CreateOrderItemRequest> items,
 @Size(max=25) String buyerPhone,
 @Size(max=500) String shippingAddress,
 @Size(max=500) String customerNote,
 @Pattern(regexp="COD|PAYOS") String paymentMethod,
 UUID requestKey
) {}
