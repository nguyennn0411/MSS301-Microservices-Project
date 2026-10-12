package com.ecommerce.payment.repository;

import com.ecommerce.payment.domain.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByOrderId(UUID orderId);

    Optional<PaymentEntity> findByOrderCode(Long orderCode);

    Optional<PaymentEntity> findByPaymentLinkId(String paymentLinkId);
}
