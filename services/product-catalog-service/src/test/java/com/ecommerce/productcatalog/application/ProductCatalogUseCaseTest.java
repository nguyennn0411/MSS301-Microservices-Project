package com.ecommerce.productcatalog.application;

import com.ecommerce.productcatalog.api.dto.ProductValidationResponse;
import com.ecommerce.productcatalog.api.dto.ValidateProductItemRequest;
import com.ecommerce.productcatalog.api.dto.ValidateProductsRequest;
import com.ecommerce.productcatalog.api.dto.InventoryReservationRequest;
import com.ecommerce.productcatalog.api.dto.InventoryReservationResponse;
import com.ecommerce.productcatalog.domain.Product;
import com.ecommerce.productcatalog.domain.ProductStatus;
import com.ecommerce.productcatalog.persistence.CategoryRepository;
import com.ecommerce.productcatalog.persistence.ProductRepository;
import com.ecommerce.productcatalog.persistence.ProductVariantRepository;
import com.ecommerce.productcatalog.persistence.InventoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
class ProductCatalogUseCaseTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @Mock
    private InventoryReservationRepository reservationRepository;

    private ProductCatalogUseCase useCase;
    private UUID productId;

    @BeforeEach
    void setUp() {
        useCase = new ProductCatalogUseCase(productRepository, categoryRepository, variantRepository, reservationRepository);
        productId = UUID.randomUUID();
    }

    @Test
    void validatesActiveProductWithMatchingPrice() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product("1000.00", ProductStatus.ACTIVE)));

        ProductValidationResponse response = useCase.validateProducts(request("1000", 2));

        assertThat(response.valid()).isTrue();
        assertThat(response.message()).isEqualTo("All products are valid");
    }

    @Test
    void rejectsChangedPrice() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product("1000.00", ProductStatus.ACTIVE)));

        ProductValidationResponse response = useCase.validateProducts(request("1200", 1));

        assertThat(response.valid()).isFalse();
        assertThat(response.message()).contains("Price changed");
    }

    @Test
    void rejectsInactiveProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product("1000.00", ProductStatus.DISCONTINUED)));

        ProductValidationResponse response = useCase.validateProducts(request("1000", 1));

        assertThat(response.valid()).isFalse();
        assertThat(response.message()).contains("not active");
    }

    @Test
    void reservesStockForSelectedVariant() {
        Product product = product("1000.00", ProductStatus.ACTIVE);
        product.setName("Essential T-Shirt");
        com.ecommerce.productcatalog.domain.ProductVariant variant = new com.ecommerce.productcatalog.domain.ProductVariant();
        UUID variantId = UUID.randomUUID();
        variant.setId(variantId);
        variant.setSize("M");
        variant.setColor("Black");
        variant.setSku("TEE-M-BLK");
        variant.setStockQuantity(5);
        product.addVariant(variant);
        when(reservationRepository.findByOrderIdOrderByCreatedAtAsc(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());
        when(variantRepository.findByIdForUpdate(variantId)).thenReturn(Optional.of(variant));
        when(reservationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryReservationResponse response = useCase.reserveInventory(new InventoryReservationRequest(
                UUID.randomUUID(), List.of(new ValidateProductItemRequest(
                productId, new BigDecimal("1000"), 2, variantId, null, null))));

        assertThat(response.reserved()).isTrue();
        assertThat(variant.getStockQuantity()).isEqualTo(3);
        verify(variantRepository).flush();
    }

    private Product product(String price, ProductStatus status) {
        Product product = new Product();
        product.setId(productId);
        product.setBasePrice(new BigDecimal(price));
        product.setStatus(status);
        return product;
    }

    private ValidateProductsRequest request(String price, int quantity) {
        return new ValidateProductsRequest(List.of(
                new ValidateProductItemRequest(productId, new BigDecimal(price), quantity, null, null, null)));
    }
}
