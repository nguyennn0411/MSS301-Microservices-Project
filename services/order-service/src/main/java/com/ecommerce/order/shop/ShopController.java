package com.ecommerce.order.shop;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.shop.dto.*;
import com.ecommerce.order.shop.repository.ShopOrderRepository;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.client.ProductCatalogClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@Profile("shop")
@RequestMapping("/api/shop")
public class ShopController {
 private final JdbcTemplate db;
 private final ObjectMapper json;
 private final OrderService service;
 private final ShopOrderRepository orders;
 private final ShopSettings settings;
 private final ProductCatalogClient catalog;
 public ShopController(JdbcTemplate db,ObjectMapper json,OrderService service,ShopOrderRepository orders,ShopSettings settings,ProductCatalogClient catalog){this.db=db;this.json=json;this.service=service;this.orders=orders;this.settings=settings;this.catalog=catalog;}
 public record CartRequest(@NotNull @Size(max=50) List<@Valid CreateOrderItemRequest> items){}
 public record Checkout(@NotBlank @Size(max=150) String buyerName,@NotBlank @Email @Size(max=200) String buyerEmail,
   @NotBlank @Pattern(regexp="[0-9+ ()-]{9,16}") String buyerPhone,@NotBlank @Size(max=500) String shippingAddress,@Size(max=500) String customerNote,
   @NotNull @Pattern(regexp="COD|PAYOS") String paymentMethod,@NotNull UUID requestKey){}
 private void ensure(UUID owner){db.update("INSERT IGNORE INTO shop_carts(user_id,items_json) VALUES (?, '[]')",owner.toString());}
 private List<CreateOrderItemRequest> readCart(UUID owner,boolean lock){
  ensure(owner);String value=db.queryForObject("SELECT items_json FROM shop_carts WHERE user_id=?"+(lock?" FOR UPDATE":""),String.class,owner.toString());
  try{return json.readValue(value,new TypeReference<>(){});}catch(Exception e){throw new IllegalStateException("Cannot read cart",e);}
 }
 private void writeCart(UUID owner,List<CreateOrderItemRequest> items){try{db.update("UPDATE shop_carts SET items_json=?,updated_at=CURRENT_TIMESTAMP(6) WHERE user_id=?",json.writeValueAsString(items),owner.toString());}catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException(e);}}
 @GetMapping("/settings") public ApiResponse<Object> settings(){return ApiResponse.ok(Map.of("shippingFee",settings.shippingFee()));}
 @GetMapping("/cart") public ApiResponse<?> cart(@RequestHeader("X-Shop-Session") UUID owner){return ApiResponse.ok(readCart(owner,false));}
 @PutMapping("/cart") @Transactional
 public ApiResponse<?> updateCart(@RequestHeader("X-Shop-Session") UUID owner,@Valid @RequestBody CartRequest request){
  readCart(owner,true);
  if(request.items().stream().anyMatch(i->i.variantId()==null))throw new IllegalArgumentException("Vui lòng chọn size và màu.");
  if(request.items().stream().map(CreateOrderItemRequest::variantId).distinct().count()!=request.items().size())throw new IllegalArgumentException("Mỗi biến thể chỉ được xuất hiện một lần.");
  if(!request.items().isEmpty()){var checked=catalog.validate(request.items());if(!checked.valid())throw new IllegalArgumentException(checked.message());}
  writeCart(owner,request.items());return ApiResponse.ok(request.items());
 }
 @PostMapping("/checkout") @Transactional
 public ApiResponse<OrderResponse> checkout(@RequestHeader("X-Shop-Session") UUID owner,@Valid @RequestBody Checkout request){
  var items=readCart(owner,true);
  var existing=orders.findByUserIdAndRequestKey(owner,request.requestKey());
  if(existing.isPresent())return ApiResponse.ok(service.toResponse(existing.get()));
  if(items.isEmpty())throw new IllegalArgumentException("Giỏ hàng đang trống.");
  var order=service.create(new CreateOrderRequest(owner,request.buyerName(),request.buyerEmail(),items,request.buyerPhone(),request.shippingAddress(),request.customerNote(),request.paymentMethod(),request.requestKey()));
  writeCart(owner,List.of());return ApiResponse.ok(order);
 }
 @GetMapping("/orders") public ApiResponse<?> history(@RequestHeader("X-Shop-Session") UUID owner){return ApiResponse.ok(orders.findByUserIdOrderByCreatedAtDesc(owner).stream().map(service::toResponse).toList());}
 private UUID ownedId(UUID owner,Long code){var order=orders.findByOrderCode(code).filter(o->o.getUserId().equals(owner)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy đơn hàng trong phiên mua sắm này."));return order.getId();}
 @GetMapping("/orders/{code}") public ApiResponse<?> find(@RequestHeader("X-Shop-Session") UUID owner,@PathVariable("code") Long code){return ApiResponse.ok(service.getById(ownedId(owner,code)));}
 public record Cancel(@Size(max=500) String reason){}
 @PostMapping("/orders/{code}/cancel") @Transactional
 public ApiResponse<?> cancel(@RequestHeader("X-Shop-Session") UUID owner,@PathVariable("code") Long code,@Valid @RequestBody Cancel request){
  UUID id=ownedId(owner,code);db.queryForList("SELECT id FROM orders WHERE id=? FOR UPDATE",id.toString());
  return ApiResponse.ok(service.cancel(id,request.reason()));
 }
}
