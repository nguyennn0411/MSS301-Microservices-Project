package com.ecommerce.order.repository;

import com.ecommerce.order.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByUserIdAndRequestKey(UUID userId, UUID requestKey);
    java.util.List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<OrderEntity> findByOrderCode(Long orderCode);

    Optional<OrderEntity> findByPaymentId(UUID paymentId);
}
