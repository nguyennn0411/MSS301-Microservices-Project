import assert from 'node:assert/strict';

const base = process.env.GATEWAY_URL || 'http://localhost:8080';
const owner = crypto.randomUUID();

async function call(path, method = 'GET', body) {
  const response = await fetch(`${base}${path}`, {
    method,
    headers: {'Content-Type': 'application/json', 'X-Shop-Session': owner},
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(20000)
  });
  const result = await response.json();
  if (!response.ok || !result.success) throw new Error(`${method} ${path}: HTTP ${response.status} ${result.message}`);
  return result.data;
}

const products = await call('/api/v1/products');
assert.ok(products.length > 0, 'Catalog is empty');
const item = {productId: products[0].id, variantId: products[0].variants[0].id, quantity: 2};
const settings = await call('/api/shop/settings');
await call('/api/shop/cart', 'PUT', {items: [item]});
assert.deepEqual(await call('/api/shop/cart'), [item]);

const order = await call('/api/shop/checkout', 'POST', {
  buyerName: 'MSS301 Gateway Test', buyerEmail: 'test@example.com',
  buyerPhone: '0900000000', shippingAddress: 'Local integration test address',
  customerNote: 'Test through Gateway', paymentMethod: 'COD',
  requestKey: crypto.randomUUID()
});
assert.equal(order.status, 'CONFIRMED');
assert.equal(Number(order.totalAmount), 2 * Number(products[0].price) + Number(settings.shippingFee));
assert.equal(order.items[0].variantId, item.variantId);
assert.equal((await call(`/api/shop/orders/${order.orderCode}`)).id, order.id);
const payment = await call(`/api/payments/order/${order.id}`);
assert.equal(payment.provider, 'COD');
assert.equal(payment.status, 'PENDING');
assert.equal(Number(payment.amount), Number(order.totalAmount));
const cancelled = await call(`/api/shop/orders/${order.orderCode}/cancel`, 'POST', {reason: 'Gateway smoke test'});
assert.equal(cancelled.status, 'CANCELLED');
assert.equal((await call(`/api/payments/order/${order.id}`)).status, 'CANCELLED');
console.log(`PASS Gateway -> Catalog -> Order -> Payment -> cancellation; orderCode=${order.orderCode}`);
