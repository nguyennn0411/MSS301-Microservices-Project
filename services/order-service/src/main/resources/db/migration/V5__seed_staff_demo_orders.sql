-- Demo data for the staff Order screen.
-- These rows are idempotent so the migration is safe to run once per database.

INSERT IGNORE INTO orders (
    id,
    user_id,
    buyer_name,
    buyer_email,
    description,
    currency,
    total_amount,
    shipping_fee,
    status,
    payment_id,
    payment_order_code,
    payment_link_id,
    checkout_url,
    qr_code,
    failure_reason,
    paid_at,
    cancelled_at,
    created_at,
    updated_at
) VALUES
    (
        '90000000-0000-0000-0000-000000000001',
        '70000000-0000-0000-0000-000000000001',
        'Nguyen Van An',
        'customer1@stepzone.local',
        '123 Le Loi Street, Ben Thanh Ward, District 1, Ho Chi Minh City | shipping=Standard Express (2-3 Days)',
        'VND',
        2000.00,
        0.00,
        'CONFIRMED',
        '91000000-0000-0000-0000-000000000001',
        1780971719001,
        'payos-demo-link-001',
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL 1 HOUR,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL 2 HOUR,
        CURRENT_TIMESTAMP - INTERVAL 1 HOUR
    ),
    (
        '90000000-0000-0000-0000-000000000002',
        '70000000-0000-0000-0000-000000000002',
        'Tran Minh Thu',
        'customer2@stepzone.local',
        '45 Nguyen Hue Street, Ben Nghe Ward, District 1, Ho Chi Minh City | shipping=Standard Delivery (3-5 Days)',
        'VND',
        3000.00,
        0.00,
        'PAYMENT_PENDING',
        '91000000-0000-0000-0000-000000000002',
        1780971719002,
        'payos-demo-link-002',
        'https://pay.payos.vn/web/demo-staff-order-002',
        NULL,
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL 25 MINUTE,
        CURRENT_TIMESTAMP - INTERVAL 25 MINUTE
    );

INSERT IGNORE INTO order_items (
    id,
    order_id,
    product_id,
    product_name,
    size,
    color,
    quantity,
    unit_price,
    line_total
) VALUES
    (
        '92000000-0000-0000-0000-000000000001',
        '90000000-0000-0000-0000-000000000001',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Adidas Samba',
        '40',
        NULL,
        2,
        1000.00,
        2000.00
    ),
    (
        '92000000-0000-0000-0000-000000000002',
        '90000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000001',
        'Nike Pegasus 41',
        '41',
        'Black',
        1,
        1000.00,
        1000.00
    ),
    (
        '92000000-0000-0000-0000-000000000003',
        '90000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000007',
        'Adidas Gazelle',
        '41',
        'Blue',
        2,
        1000.00,
        2000.00
    );

INSERT IGNORE INTO saga_transaction_logs (
    id,
    order_id,
    step,
    status,
    message,
    created_at
) VALUES
    (
        '93000000-0000-0000-0000-000000000001',
        '90000000-0000-0000-0000-000000000001',
        'ORDER_CREATED',
        'SUCCESS',
        'Demo order was created by customer1@stepzone.local',
        CURRENT_TIMESTAMP - INTERVAL 2 HOUR
    ),
    (
        '93000000-0000-0000-0000-000000000003',
        '90000000-0000-0000-0000-000000000001',
        'PAYMENT_SUCCESS_RECEIVED',
        'SUCCESS',
        'Payment success event was received from Payment Service',
        CURRENT_TIMESTAMP - INTERVAL 1 HOUR
    ),
    (
        '93000000-0000-0000-0000-000000000004',
        '90000000-0000-0000-0000-000000000001',
        'ORDER_CONFIRMED',
        'SUCCESS',
        'Order was confirmed after payment success',
        CURRENT_TIMESTAMP - INTERVAL 59 MINUTE
    ),
    (
        '93000000-0000-0000-0000-000000000005',
        '90000000-0000-0000-0000-000000000002',
        'ORDER_CREATED',
        'SUCCESS',
        'Demo order was created by customer2@stepzone.local',
        CURRENT_TIMESTAMP - INTERVAL 25 MINUTE
    ),
    (
        '93000000-0000-0000-0000-000000000006',
        '90000000-0000-0000-0000-000000000002',
        'PAYMENT_CREATED',
        'SUCCESS',
        'PayOS payment link was created and is waiting for payment',
        CURRENT_TIMESTAMP - INTERVAL 24 MINUTE
    );
