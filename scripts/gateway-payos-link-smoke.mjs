import assert from 'node:assert/strict';

const base = process.env.GATEWAY_URL || 'http://127.0.0.1:8080';
const owner = crypto.randomUUID();

async function call(path, method = 'GET', body) {
  const response = await fetch(`${base}${path}`, {
    method,
    headers: {'Content-Type': 'application/json', 'X-Shop-Session': owner},
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(30000)
  });
  const result = await response.json();
  if (!response.ok || !result.success) {
    throw new Error(`${method} ${path}: HTTP ${response.status} ${result.message}`);
  }
  return result.data;
}

const capabilities = await call('/api/payments/capabilities');
assert.equal(capabilities.payosEnabled, true, 'PayOS is not enabled');
const products = await call('/api/v1/products');
const product = products.find(value => value.variants?.length && Number(value.price) > 0);
assert.ok(product, 'No purchasable product is available');
await call('/api/shop/cart', 'PUT', {
  items: [{productId: product.id, variantId: product.variants[0].id, quantity: 1}]
});
const order = await call('/api/shop/checkout', 'POST', {
  buyerName: 'MSS301 PayOS Link Test', buyerEmail: 'test@example.com',
  buyerPhone: '0900000000', shippingAddress: 'Local integration test address',
  paymentMethod: 'PAYOS', requestKey: crypto.randomUUID()
});
assert.equal(order.status, 'PENDING_PAYMENT');
assert.ok(order.checkoutUrl?.startsWith('https://'), 'Missing PayOS checkout URL');
const payment = await call(`/api/payments/order/${order.id}`);
assert.equal(payment.provider, 'PAYOS');
assert.equal(payment.status, 'PENDING');
const cancelled = await call(`/api/shop/orders/${order.orderCode}/cancel`, 'POST', {
  reason: 'PayOS link integration test'
});
assert.equal(cancelled.status, 'CANCELLED');
assert.equal((await call(`/api/payments/order/${order.id}`)).status, 'CANCELLED');
console.log(`PASS PayOS link creation and cancellation; orderCode=${order.orderCode}`);
