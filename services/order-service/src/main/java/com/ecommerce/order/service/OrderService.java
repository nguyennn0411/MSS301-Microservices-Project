package com.ecommerce.order.service;

import com.ecommerce.order.client.*;
import com.ecommerce.order.client.dto.*;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.shop.domain.*;
import com.ecommerce.order.shop.dto.*;
import com.ecommerce.order.shop.repository.ShopOrderRepository;
import com.ecommerce.order.shop.ShopSettings;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.*;
import java.math.BigDecimal;

@Service
@Profile("shop")
public class OrderService {
 private final ShopOrderRepository orders;
 private final ProductCatalogClient catalog;
 private final PaymentClient payments;
 private final OrderCodeGenerator codes;
 private final ShopSettings settings;
 public OrderService(ShopOrderRepository orders,ProductCatalogClient catalog,PaymentClient payments,OrderCodeGenerator codes,ShopSettings settings){this.orders=orders;this.catalog=catalog;this.payments=payments;this.codes=codes;this.settings=settings;}
 public OrderResponse create(CreateOrderRequest request){
  if(request.requestKey()!=null){var existing=orders.findByUserIdAndRequestKey(request.userId(),request.requestKey());if(existing.isPresent())return toResponse(existing.get());}
  String method=request.paymentMethod()==null?"PAYOS":request.paymentMethod();
  if(!Set.of("COD","PAYOS").contains(method))throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
  if(method.equals("PAYOS")&&!payments.payosEnabled())throw new IllegalArgumentException("PayOS chưa được cấu hình. Vui lòng chọn COD.");
  if(request.items().stream().map(i->i.productId()+":"+i.variantId()).distinct().count()!=request.items().size())throw new IllegalArgumentException("Biến thể sản phẩm bị trùng.");
  var checked=catalog.validate(request.items());
  if(!checked.valid())throw new IllegalArgumentException(checked.message());
  OrderEntity order=OrderEntity.create(codes.next(),request.userId(),request.buyerName().trim(),request.buyerEmail().trim(),"VND");
  order.setDelivery(request.buyerPhone(),request.shippingAddress(),request.customerNote(),method,request.shippingAddress()==null?BigDecimal.ZERO:settings.shippingFee(),request.requestKey());
  for(var item:request.items()){
   var validated=checked.items().stream().filter(v->v.productId().equals(item.productId())&&Objects.equals(v.variantId(),item.variantId())).findFirst().orElseThrow(()->new IllegalArgumentException("Thiếu thông tin sản phẩm."));
   if(!validated.available()||validated.unitPrice()==null||validated.unitPrice().signum()<0)throw new IllegalArgumentException("Sản phẩm không khả dụng.");
   var entity=OrderItemEntity.create(item.productId(),validated.productName(),validated.unitPrice(),item.quantity());
   entity.setVariant(validated.variantId(),validated.size(),validated.color());order.addItem(entity);
  }
  orders.saveAndFlush(order);
  try {
   var payment=payments.createPayment(method,new CreatePaymentRequest(order.getId(),order.getOrderCode(),order.getUserId(),order.getBuyerName(),order.getBuyerEmail(),order.getTotalAmount(),order.getCurrency()));
   order.attachPayment(payment.id(),payment.paymentLinkId(),payment.checkoutUrl());
   if("FAILED".equals(payment.status()))order.markPaymentFailed("Payment initialization failed");
   else if(method.equals("COD"))order.confirmCod();
  }catch(RuntimeException ex){order.markPaymentFailed("Không thể khởi tạo thanh toán. Vui lòng liên hệ cửa hàng với mã đơn.");}
  orders.save(order);return toResponse(order);
 }
 public OrderResponse getById(UUID id){return toResponse(findById(id));}
 public OrderResponse getByOrderCode(Long code){return toResponse(findByOrderCode(code));}
 public OrderResponse cancel(UUID id,String reason){
  var order=findById(id);
  if(order.getStatus()==OrderStatus.CANCELLED)return toResponse(order);
  if(order.getStatus()==OrderStatus.PAID||order.getStatus()==OrderStatus.REFUNDED)throw new IllegalArgumentException("Không thể hủy đơn đã thanh toán hoặc hoàn tiền.");
  String why=reason==null||reason.isBlank()?"Cancelled by customer":reason.trim();
  if(order.getPaymentId()!=null)payments.cancelPayOSPayment(order.getOrderCode(),why);
  order.markCancelled(why);orders.save(order);return toResponse(order);
 }
 public OrderEntity findByOrderCode(Long code){return orders.findByOrderCode(code).orElseThrow(()->new IllegalArgumentException("Order not found: "+code));}
 public OrderEntity findById(UUID id){return orders.findById(id).orElseThrow(()->new IllegalArgumentException("Order not found: "+id));}
 public void save(OrderEntity order){orders.save(order);}
 public OrderResponse toResponse(OrderEntity o){return new OrderResponse(o.getId(),o.getOrderCode(),o.getUserId(),o.getBuyerName(),o.getBuyerEmail(),o.getTotalAmount(),o.getCurrency(),o.getStatus(),o.getPaymentId(),o.getPaymentLinkId(),o.getCheckoutUrl(),o.getStatusReason(),o.getItems().stream().map(i->new OrderItemResponse(i.getId(),i.getProductId(),i.getProductName(),i.getUnitPrice(),i.getQuantity(),i.getLineTotal(),i.getVariantId(),i.getSize(),i.getColor())).toList(),o.getCreatedAt(),o.getUpdatedAt(),o.getBuyerPhone(),o.getShippingAddress(),o.getCustomerNote(),o.getPaymentMethod(),o.getShippingFee());}
}
