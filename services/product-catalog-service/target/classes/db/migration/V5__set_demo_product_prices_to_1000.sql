-- Keep demo checkout amounts small and consistent across the product catalog.
UPDATE products
SET base_price = 1000.00,
    updated_at = CURRENT_TIMESTAMP;
