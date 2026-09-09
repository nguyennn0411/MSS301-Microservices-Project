-- Existing developer databases may already have prior demo-price migrations.
-- Reapply the intended demo price without rewriting those applied migrations.
UPDATE products
SET base_price = 1000.00,
    updated_at = CURRENT_TIMESTAMP;
