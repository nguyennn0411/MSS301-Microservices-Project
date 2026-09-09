-- Keep the pending staff demo order fresh so it does not expire immediately on a new demo database.

UPDATE orders
SET
    status = 'PAYMENT_PENDING',
    failure_reason = NULL,
    cancelled_at = NULL,
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE id = '90000000-0000-0000-0000-000000000002';

UPDATE saga_transaction_logs
SET created_at = CURRENT_TIMESTAMP
WHERE order_id = '90000000-0000-0000-0000-000000000002'
  AND step IN ('ORDER_CREATED', 'PAYMENT_CREATED');
