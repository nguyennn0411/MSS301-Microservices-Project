package com.ecommerce.productcatalog.persistence;

import com.ecommerce.productcatalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    @Query("select p from Product p left join fetch p.category where p.id = :id")
    Optional<Product> findWithCategoryById(@Param("id") UUID id);
}
