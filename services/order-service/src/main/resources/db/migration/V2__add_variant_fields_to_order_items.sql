ALTER TABLE order_items
    ADD COLUMN size VARCHAR(50) NULL,
    ADD COLUMN color VARCHAR(100) NULL;

UPDATE order_items
SET size = 'UNKNOWN'
WHERE size IS NULL OR size = '';

ALTER TABLE order_items
    MODIFY COLUMN size VARCHAR(50) NOT NULL;
