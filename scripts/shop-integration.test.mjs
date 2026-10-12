import {test} from 'node:test';
import assert from 'node:assert/strict';
const base='http://localhost:5173';
async function session(){const response=await fetch(base);return response.headers.get('set-cookie').split(';')[0];}
async function call(cookie,service,path,method='GET',body){
 const r=await fetch(`${base}/backend/${service}${path}`,{method,headers:{Cookie:cookie,'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body)});
 return {status:r.status,...await r.json()};
}
test('MySQL storefront: catalog, cart, checkout, payment, ownership, replay and cancel',async()=>{
 const a=await session(),b=await session();
 const catalog=await call(a,'product','/api/v1/products');assert.equal(catalog.status,200);assert.ok(catalog.data.length>=2);
 const [p,q]=catalog.data;assert.ok(p.variants.length>0);
 const settings=await call(a,'order','/api/shop/settings');
 const items=[{productId:p.id,variantId:p.variants[0].id,quantity:2}];
 let r=await call(a,'order','/api/shop/cart','PUT',{items});assert.equal(r.status,200);
 r=await call(a,'order','/api/shop/cart');assert.deepEqual(r.data,items);
 r=await call(b,'order','/api/shop/cart');assert.deepEqual(r.data,[]);
 r=await call(a,'order','/api/shop/cart','PUT',{items:[{productId:p.id,variantId:q.variants[0].id,quantity:1}]});assert.equal(r.status,400);
 r=await call(a,'order','/api/shop/cart','PUT',{items:[{...items[0],quantity:0}]});assert.equal(r.status,400);
 r=await call(a,'order','/api/shop/cart','PUT',{items:[items[0],items[0]]});assert.equal(r.status,400);
 const request={buyerName:'Integration Test MSS',buyerEmail:'integration@example.com',buyerPhone:'0901234567',shippingAddress:'Dia chi kiem thu local',customerNote:'Automated integration check',paymentMethod:'COD',requestKey:crypto.randomUUID(),amount:1,totalAmount:1};
 const created=await call(a,'order','/api/shop/checkout','POST',request);assert.equal(created.status,200,JSON.stringify(created));
 const order=created.data;assert.equal(order.status,'CONFIRMED');assert.equal(order.totalAmount,2*p.price+Number(settings.data.shippingFee));assert.equal(order.buyerPhone,request.buyerPhone);assert.equal(order.items[0].variantId,p.variants[0].id);
 const replay=await call(a,'order','/api/shop/checkout','POST',request);assert.equal(replay.data.id,order.id);
 r=await call(a,'order','/api/shop/cart');assert.deepEqual(r.data,[]);
 r=await call(b,'order',`/api/shop/orders/${order.orderCode}`);assert.equal(r.status,404);
 r=await call(a,'order','/api/shop/orders');assert.equal(r.data.filter(x=>x.id===order.id).length,1);
 r=await call(a,'payment',`/api/payments/order/${order.id}`);assert.equal(r.data.provider,'COD');assert.equal(r.data.status,'PENDING');assert.equal(r.data.amount,order.totalAmount);
 r=await call(a,'order',`/api/shop/orders/${order.orderCode}/cancel`,'POST',{reason:'Integration test complete'});assert.equal(r.data.status,'CANCELLED');
 r=await call(a,'payment',`/api/payments/order/${order.id}`);assert.equal(r.data.status,'CANCELLED');
 const capabilities=await call(a,'payment','/api/payments/capabilities');
 if(!capabilities.data.payosEnabled){
  await call(a,'order','/api/shop/cart','PUT',{items});
  r=await call(a,'order','/api/shop/checkout','POST',{...request,paymentMethod:'PAYOS',requestKey:crypto.randomUUID()});assert.equal(r.status,400);
  r=await call(a,'order','/api/shop/cart');assert.deepEqual(r.data,items);
  await call(a,'order','/api/shop/cart','PUT',{items:[]});
 }
 console.log('Verified database-backed order',order.orderCode,order.id,'total',order.totalAmount,'VND');
});
