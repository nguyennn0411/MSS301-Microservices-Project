package com.ecommerce.productcatalog.application;

import com.ecommerce.productcatalog.api.dto.CategoryResponse;
import com.ecommerce.productcatalog.api.dto.CreateCategoryRequest;
import com.ecommerce.productcatalog.api.dto.CreateProductRequest;
import com.ecommerce.productcatalog.api.dto.ProductImageRequest;
import com.ecommerce.productcatalog.api.dto.ProductImageResponse;
import com.ecommerce.productcatalog.api.dto.ProductResponse;
import com.ecommerce.productcatalog.api.dto.ProductValidationResponse;
import com.ecommerce.productcatalog.api.dto.ProductVariantRequest;
import com.ecommerce.productcatalog.api.dto.ProductVariantResponse;
import com.ecommerce.productcatalog.api.dto.InventoryReservationItemResponse;
import com.ecommerce.productcatalog.api.dto.InventoryReservationRequest;
import com.ecommerce.productcatalog.api.dto.InventoryReservationResponse;
import com.ecommerce.productcatalog.api.dto.UpdateProductRequest;
import com.ecommerce.productcatalog.api.dto.ValidateProductItemRequest;
import com.ecommerce.productcatalog.api.dto.ValidateProductsRequest;
import com.ecommerce.productcatalog.domain.Category;
import com.ecommerce.productcatalog.domain.Product;
import com.ecommerce.productcatalog.domain.ProductImage;
import com.ecommerce.productcatalog.domain.ProductStatus;
import com.ecommerce.productcatalog.domain.ProductVariant;
import com.ecommerce.productcatalog.domain.InventoryReservation;
import com.ecommerce.productcatalog.persistence.CategoryRepository;
import com.ecommerce.productcatalog.persistence.ProductRepository;
import com.ecommerce.productcatalog.persistence.ProductSpecifications;
import com.ecommerce.productcatalog.persistence.ProductVariantRepository;
import com.ecommerce.productcatalog.persistence.InventoryReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
@Transactional
public class ProductCatalogUseCase {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryReservationRepository reservationRepository;

    public ProductCatalogUseCase(ProductRepository productRepository, CategoryRepository categoryRepository,
                                 ProductVariantRepository variantRepository,
                                 InventoryReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.reservationRepository = reservationRepository;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category name already exists");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(trimToNull(request.description()));
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCategoryResponse)
                .toList();
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        applyProductFields(product, request.name(), request.brand(), request.description(), request.categoryId(),
                request.basePrice(), parseStatus(request.status(), ProductStatus.ACTIVE));
        replaceVariants(product, request.variants());
        replaceImages(product, request.images());
        return toProductResponse(productRepository.saveAndFlush(product));
    }

    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = loadProduct(id);
        applyProductFields(product, request.name(), request.brand(), request.description(), request.categoryId(),
                request.basePrice(), parseStatus(request.status(), product.getStatus()));
        replaceVariants(product, request.variants());
        replaceImages(product, request.images());
        return toProductResponse(productRepository.saveAndFlush(product));
    }

    public ProductResponse discontinueProduct(UUID id) {
        Product product = loadProduct(id);
        product.setStatus(ProductStatus.DISCONTINUED);
        return toProductResponse(productRepository.saveAndFlush(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID id) {
        return toProductResponse(loadProduct(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword, UUID categoryId, String status) {
        ProductStatus parsedStatus = status == null || status.isBlank() ? null : parseStatus(status, null);
        return productRepository.findAll(ProductSpecifications.withFilters(keyword, categoryId, parsedStatus))
                .stream().map(this::toProductResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductValidationResponse validateProducts(ValidateProductsRequest request) {
        for (ValidateProductItemRequest item : request.items()) {
            ProductValidationResponse response = validateItem(item);
            if (!response.valid()) return response;
        }
        return new ProductValidationResponse(true, "All products are valid");
    }

    public InventoryReservationResponse reserveInventory(InventoryReservationRequest request) {
        List<InventoryReservation> existing = reservationRepository.findByOrderIdOrderByCreatedAtAsc(request.orderId());
        if (existing.stream().anyMatch(item -> item.getStatus() == InventoryReservation.Status.RESERVED)) {
            return reservationResponse(request.orderId(), existing, "Inventory was already reserved");
        }

        Set<UUID> selectedVariants = new HashSet<>();
        List<InventoryReservation> reservations = request.items().stream().map(item -> {
            ProductVariant variant = lockVariant(item);
            Product product = variant.getProduct();
            validateProductAndPrice(product, item);
            if (!selectedVariants.add(variant.getId())) {
                throw new IllegalArgumentException("The same variant cannot appear more than once in an order");
            }
            variant.reserve(item.quantity());
            return new InventoryReservation(request.orderId(), variant, item.quantity());
        }).toList();
        variantRepository.flush();
        reservationRepository.saveAll(reservations);
        return reservationResponse(request.orderId(), reservations, "Inventory reserved");
    }

    public InventoryReservationResponse releaseInventory(UUID orderId) {
        List<InventoryReservation> reservations = reservationRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        List<InventoryReservation> active = reservations.stream()
                .filter(item -> item.getStatus() == InventoryReservation.Status.RESERVED)
                .toList();
        if (active.isEmpty()) {
            return new InventoryReservationResponse(orderId, false, "No active reservation", List.of());
        }
        for (InventoryReservation reservation : active) {
            ProductVariant variant = variantRepository.findByIdForUpdate(reservation.getVariant().getId())
                    .orElseThrow(() -> new ProductNotFoundException("Reserved variant was not found"));
            variant.release(reservation.getQuantity());
            reservation.release();
        }
        return reservationResponse(orderId, active, "Inventory released");
    }

    private ProductValidationResponse validateItem(ValidateProductItemRequest item) {
        Product product = productRepository.findById(item.productId()).orElse(null);
        if (product == null) {
            return new ProductValidationResponse(false, "Product %s was not found".formatted(item.productId()));
        }
        try {
            validateProductAndPrice(product, item);
            ProductVariant variant = findVariant(product, item);
            if (variant != null && variant.getStockQuantity() < item.quantity()) {
                return new ProductValidationResponse(false, "Insufficient stock for variant %s".formatted(variant.getId()));
            }
        } catch (IllegalArgumentException exception) {
            return new ProductValidationResponse(false, exception.getMessage());
        }
        return new ProductValidationResponse(true, "Product is valid");
    }

    private void validateProductAndPrice(Product product, ValidateProductItemRequest item) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Product %s is not active".formatted(item.productId()));
        }
        if (product.getBasePrice().compareTo(item.unitPrice()) != 0) {
            throw new IllegalArgumentException("Price changed for product %s".formatted(item.productId()));
        }
    }

    private ProductVariant findVariant(Product product, ValidateProductItemRequest item) {
        if (item.variantId() != null) {
            return product.getVariants().stream().filter(variant -> variant.getId().equals(item.variantId()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Variant does not belong to product " + item.productId()));
        }
        if (item.size() == null || item.size().isBlank()) return null;
        return product.getVariants().stream()
                .filter(variant -> variant.getSize().equalsIgnoreCase(item.size().trim()))
                .filter(variant -> colorsEqual(variant.getColor(), item.color()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Product variant was not found"));
    }

    private ProductVariant lockVariant(ValidateProductItemRequest item) {
        ProductVariant variant;
        if (item.variantId() != null) {
            variant = variantRepository.findByIdForUpdate(item.variantId())
                    .orElseThrow(() -> new ProductNotFoundException("Variant %s was not found".formatted(item.variantId())));
        } else {
            if (item.size() == null || item.size().isBlank()) {
                throw new IllegalArgumentException("variantId or size is required for inventory reservation");
            }
            variant = variantRepository.findForUpdate(item.productId(), item.size().trim(), trimToNull(item.color()))
                    .orElseThrow(() -> new ProductNotFoundException("Product variant was not found"));
        }
        if (!variant.getProduct().getId().equals(item.productId())) {
            throw new IllegalArgumentException("Variant does not belong to product " + item.productId());
        }
        return variant;
    }

    private boolean colorsEqual(String left, String right) {
        String normalizedRight = trimToNull(right);
        return left == null ? normalizedRight == null : normalizedRight != null && left.equalsIgnoreCase(normalizedRight);
    }

    private Product loadProduct(UUID id) {
        return productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product %s was not found".formatted(id)));
    }

    private void applyProductFields(Product product, String name, String brand, String description,
                                    UUID categoryId, BigDecimal basePrice, ProductStatus status) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ProductNotFoundException("Category %s was not found".formatted(categoryId)));
        product.setName(name.trim());
        product.setBrand(trimToNull(brand));
        product.setDescription(trimToNull(description));
        product.setCategory(category);
        product.setBasePrice(basePrice);
        product.setStatus(status);
    }

    private void replaceVariants(Product product, List<ProductVariantRequest> requests) {
        if (requests == null) return;

        Set<String> uniqueVariants = new HashSet<>();
        Map<String, ProductVariant> existing = new HashMap<>();
        for (ProductVariant variant : product.getVariants()) {
            existing.put(variantKey(variant.getSize(), variant.getColor()), variant);
        }
        Set<ProductVariant> retained = new HashSet<>();
        for (ProductVariantRequest request : requests) {
            String size = request.size().trim().toUpperCase(Locale.ROOT);
            String color = normalizeColor(request.color());
            String key = variantKey(size, color);
            if (!uniqueVariants.add(key)) {
                throw new IllegalArgumentException("Duplicate size/color variant: " + size + "/" + color);
            }
            ProductVariant variant = existing.getOrDefault(key, new ProductVariant());
            variant.setSize(size);
            variant.setColor(color);
            variant.setSku(trimToNull(request.sku()));
            if (request.stockQuantity() != null) variant.setStockQuantity(request.stockQuantity());
            if (variant.getProduct() == null) product.addVariant(variant);
            retained.add(variant);
        }
        product.getVariants().removeIf(variant -> !retained.contains(variant));
    }

    private void replaceImages(Product product, List<ProductImageRequest> requests) {
        product.clearImages();
        if (requests == null) return;
        long mainCount = requests.stream().filter(image -> Boolean.TRUE.equals(image.main())).count();
        if (mainCount > 1) throw new IllegalArgumentException("Only one product image can be marked as main");

        for (ProductImageRequest request : requests) {
            ProductImage image = new ProductImage();
            image.setImageUrl(request.imageUrl().trim());
            image.setMain(Boolean.TRUE.equals(request.main()));
            product.addImage(image);
        }
    }

    private ProductResponse toProductResponse(Product product) {
        Category category = product.getCategory();
        List<ProductVariantResponse> variants = product.getVariants().stream()
                .sorted(Comparator.comparing(ProductVariant::getSize)
                        .thenComparing(variant -> variant.getColor() == null ? "" : variant.getColor()))
                .map(variant -> new ProductVariantResponse(variant.getId(), variant.getSize(), variant.getColor(),
                        variant.getSku(), variant.getStockQuantity()))
                .toList();
        List<ProductImageResponse> images = product.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::isMain).reversed())
                .map(image -> new ProductImageResponse(image.getId(), image.getImageUrl(), image.isMain()))
                .toList();
        return new ProductResponse(product.getId(), product.getName(), product.getBrand(), product.getDescription(),
                category == null ? null : category.getId(), category == null ? null : category.getName(),
                product.getBasePrice(), product.getStatus().name(), variants, images,
                product.getCreatedAt(), product.getUpdatedAt());
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(),
                category.getCreatedAt(), category.getUpdatedAt());
    }

    private ProductStatus parseStatus(String value, ProductStatus fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return ProductStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("status must be ACTIVE, INACTIVE, or DISCONTINUED");
        }
    }

    private String normalizeColor(String value) {
        String color = trimToNull(value);
        if (color == null) return null;
        return color.substring(0, 1).toUpperCase(Locale.ROOT) + color.substring(1).toLowerCase(Locale.ROOT);
    }

    private String variantKey(String size, String color) {
        return size.toUpperCase(Locale.ROOT) + "|" + (color == null ? "" : color.toLowerCase(Locale.ROOT));
    }

    private InventoryReservationResponse reservationResponse(UUID orderId, List<InventoryReservation> reservations,
                                                             String message) {
        List<InventoryReservationItemResponse> items = reservations.stream().map(reservation -> {
            ProductVariant variant = reservation.getVariant();
            Product product = variant.getProduct();
            return new InventoryReservationItemResponse(product.getId(), product.getName(), variant.getId(),
                    variant.getSize(), variant.getColor(), product.getBasePrice(), reservation.getQuantity());
        }).toList();
        return new InventoryReservationResponse(orderId, true, message, items);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
