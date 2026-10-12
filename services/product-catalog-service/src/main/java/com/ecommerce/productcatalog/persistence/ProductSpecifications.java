package com.ecommerce.productcatalog.persistence;

import com.ecommerce.productcatalog.domain.Product;
import com.ecommerce.productcatalog.domain.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ProductSpecifications {
    private ProductSpecifications() {
    }

    public static Specification<Product> withFilters(String keyword, UUID categoryId, ProductStatus status) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("brand")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern)
                ));
            }
            if (categoryId != null) predicates.add(builder.equal(root.get("category").get("id"), categoryId));
            if (status != null) predicates.add(builder.equal(root.get("status"), status));
            query.orderBy(builder.desc(root.get("createdAt")));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
