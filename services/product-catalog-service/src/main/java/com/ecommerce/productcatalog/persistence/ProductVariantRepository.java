package com.ecommerce.productcatalog.persistence;

import com.ecommerce.productcatalog.domain.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductVariant v join fetch v.product where v.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductVariant v join fetch v.product where v.product.id = :productId " +
            "and upper(v.size) = upper(:size) and ((:color is null and v.color is null) or upper(v.color) = upper(:color))")
    Optional<ProductVariant> findForUpdate(@Param("productId") UUID productId,
                                           @Param("size") String size,
                                           @Param("color") String color);
}
