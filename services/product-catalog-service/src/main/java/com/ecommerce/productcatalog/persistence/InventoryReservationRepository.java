package com.ecommerce.productcatalog.persistence;

import com.ecommerce.productcatalog.domain.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {
    List<InventoryReservation> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
    boolean existsByOrderIdAndStatus(UUID orderId, InventoryReservation.Status status);
}
