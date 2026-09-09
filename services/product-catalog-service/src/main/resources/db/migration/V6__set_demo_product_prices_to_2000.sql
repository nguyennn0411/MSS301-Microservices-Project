-- Retained for databases that applied the earlier 2000-VND demo price migration.
-- V7 below sets the final intended price for all environments.
UPDATE products
SET base_price = 2000.00,
    updated_at = CURRENT_TIMESTAMP;
